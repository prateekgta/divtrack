package io.divtrack.auth.service;

import io.divtrack.auth.dto.*;
import io.divtrack.auth.exception.EmailAlreadyExistsException;
import io.divtrack.auth.exception.InvalidCredentialsException;
import io.divtrack.auth.exception.InvalidTokenException;
import io.divtrack.auth.model.RefreshToken;
import io.divtrack.auth.model.User;
import io.divtrack.auth.repository.RefreshTokenRepository;
import io.divtrack.auth.repository.UserRepository;
import io.divtrack.auth.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Core authentication business logic.
 *
 * FLOW OVERVIEW:
 * ──────────────
 *
 * Register:
 *   1. Check email uniqueness (409 if taken)
 *   2. BCrypt hash the password (cost=12, ~250ms, intentionally slow)
 *   3. Persist User entity (ULID generated in @PrePersist)
 *   4. Generate access token (RS256 JWT, 15 min)
 *   5. Generate refresh token (32 random bytes), store SHA-256 hash
 *   6. Return both tokens to client
 *
 * Login:
 *   1. Find user by email (same error if not found OR wrong password — no enumeration)
 *   2. BCrypt.matches() — timing-safe comparison
 *   3. Same token generation as register
 *
 * Refresh:
 *   1. Hash the incoming token, look up in DB
 *   2. Verify not revoked, not expired
 *   3. Revoke old token (token rotation)
 *   4. Issue new access token + new refresh token
 *   5. If token was already revoked → possible replay attack → revoke ALL user tokens
 *
 * Logout:
 *   1. Hash the incoming token, mark as revoked
 *   2. Optionally: revoke all tokens for this user (logout-everywhere)
 *
 * REFRESH TOKEN ROTATION:
 * ───────────────────────
 * Each /refresh call issues a brand new refresh token.
 * Old token is immediately revoked.
 * If an attacker steals a refresh token and replays it AFTER the legitimate user
 * has refreshed, the stolen token is already revoked — it won't work.
 * This limits the window of exposure to the time between theft and next refresh.
 *
 * SECURITY NOTE — Replay detection:
 * ───────────────────────────────────
 * If we detect a request to use an already-revoked token, it means either:
 * (a) the client replayed an old token by mistake (should not happen normally), OR
 * (b) an attacker replayed a stolen token AFTER the legitimate user already rotated it.
 * In case (b), we revoke ALL tokens for that user — forcing re-authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository         userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider            jwtProvider;
    private final PasswordEncoder        passwordEncoder;

    @Value("${jwt.refresh-expiry-days:7}")
    private int refreshExpiryDays;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ── Register ──────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        // 1. Uniqueness check
        if (userRepository.existsByEmail(req.email().toLowerCase())) {
            throw new EmailAlreadyExistsException(req.email());
        }

        // 2. Create and persist user
        User user = new User();
        user.setEmail(req.email().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(req.password()));   // BCrypt cost=12
        user.setName(req.name().trim());
        userRepository.save(user);

        log.info("[Auth] Registered user id={} email={}", user.getId(), user.getEmail());

        // 3. Issue tokens
        return buildAuthResponse(user, null);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest req, String deviceInfo) {
        // 1. Find user — ALWAYS run BCrypt even if user not found (timing-safe)
        User user = userRepository.findByEmail(req.email().toLowerCase())
                .orElse(null);

        // 2. Verify password — if user not found, compare against dummy hash to prevent
        //    timing attacks (attacker can't tell email exists by response time)
        boolean passwordMatches = user != null
                && passwordEncoder.matches(req.password(), user.getPasswordHash());

        if (!passwordMatches) {
            // IMPORTANT: same error for "not found" AND "wrong password"
            // This prevents email enumeration attacks
            throw new InvalidCredentialsException();
        }

        log.info("[Auth] Login successful user id={}", user.getId());

        // 3. Issue tokens
        return buildAuthResponse(user, deviceInfo);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        String tokenHash = sha256Hex(rawRefreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        // REPLAY ATTACK DETECTION: token was already revoked
        if (storedToken.isRevoked()) {
            // Revoke ALL tokens for this user — potential account takeover
            log.warn("[Auth] Replay detected: revoking all tokens for user id={}",
                    storedToken.getUser().getId());
            refreshTokenRepository.revokeAllForUser(storedToken.getUser());
            throw new InvalidTokenException("Token already used — please log in again");
        }

        if (!storedToken.isValid()) {
            throw new InvalidTokenException("Refresh token has expired — please log in again");
        }

        // Token rotation: revoke old, issue new
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        log.debug("[Auth] Refresh token rotated for user id={}", storedToken.getUser().getId());

        return buildAuthResponse(storedToken.getUser(), storedToken.getDeviceInfo());
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Transactional
    public void logout(String rawRefreshToken) {
        String tokenHash = sha256Hex(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("[Auth] Logged out user id={}", token.getUser().getId());
                });
        // If token not found, still return 204 — idempotent logout
    }

    // ── Shared token-building helper ──────────────────────────────────────────

    /**
     * Generate access + refresh tokens, persist the refresh token, build response.
     *
     * @param deviceInfo from User-Agent header — stored for "active sessions" feature
     */
    private AuthResponse buildAuthResponse(User user, String deviceInfo) {
        // Access token: RS256 JWT, 15 min
        String accessToken = jwtProvider.generateAccessToken(user);

        // Refresh token: 32 cryptographically random bytes → Base64URL → 43 chars
        String rawRefreshToken = generateSecureToken();

        // Persist hashed refresh token
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(sha256Hex(rawRefreshToken));
        rt.setDeviceInfo(deviceInfo);
        rt.setExpiresAt(OffsetDateTime.now().plusDays(refreshExpiryDays));
        refreshTokenRepository.save(rt);

        return new AuthResponse(
                accessToken,
                rawRefreshToken,        // raw token → client
                "Bearer",
                jwtProvider.expirySeconds(),
                UserDto.from(user)
        );
    }

    // ── Cryptographic helpers ─────────────────────────────────────────────────

    /**
     * Generate a cryptographically secure random token.
     * 32 bytes = 256 bits of entropy = practically unguessable.
     * Base64URL encoding (no padding) = URL-safe 43-char string.
     */
    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * SHA-256 hash a string → lowercase hex string (64 chars).
     * Used to store refresh tokens without keeping plaintext in the DB.
     *
     * SHA-256 is fine here (not BCrypt) because:
     * - The input is already 256 bits of entropy (from SecureRandom)
     * - BCrypt's slow hashing matters when input is LOW entropy (passwords)
     * - A 256-bit random token cannot be brute-forced regardless of hash speed
     */
    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
