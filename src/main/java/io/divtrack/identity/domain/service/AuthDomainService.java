package io.divtrack.identity.domain.service;

import io.divtrack.identity.domain.model.RefreshToken;
import io.divtrack.identity.domain.model.User;
import io.divtrack.identity.domain.port.RefreshTokenRepository;
import io.divtrack.identity.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthDomainService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-expiry-days:7}")
    private int refreshExpiryDays;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public User register(String email, String password, String name) {
        String normalizedEmail = email.toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(email);
        }
        String hash = passwordEncoder.encode(password);
        User user = new User(normalizedEmail, hash, name.trim());
        return userRepository.save(user);
    }

    public User login(String email, String password) {
        String normalizedEmail = email.toLowerCase().trim();
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);
        boolean matches = user != null && passwordEncoder.matches(password, user.getPasswordHash());
        if (!matches) {
            throw new InvalidCredentialsException();
        }
        return user;
    }

    public record TokenPair(String rawToken, RefreshToken entity) {}
    public record RefreshResult(User user, TokenPair newTokens) {}

    public TokenPair generateRefreshToken(User user, String deviceInfo) {
        String raw = generateSecureToken();
        String hash = sha256Hex(raw);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hash);
        rt.setDeviceInfo(deviceInfo);
        rt.setExpiresAt(OffsetDateTime.now().plusDays(refreshExpiryDays));
        refreshTokenRepository.save(rt);

        return new TokenPair(raw, rt);
    }

    public RefreshResult rotateRefreshToken(String rawToken) {
        String hash = sha256Hex(rawToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (stored.isRevoked()) {
            refreshTokenRepository.revokeAllForUser(stored.getUser());
            throw new InvalidTokenException("Token already used — re-authentication required");
        }

        if (!stored.isValid()) {
            throw new InvalidTokenException("Refresh token expired");
        }

        stored.revoke();
        refreshTokenRepository.save(stored);

        TokenPair newTokens = generateRefreshToken(stored.getUser(), stored.getDeviceInfo());
        return new RefreshResult(stored.getUser(), newTokens);
    }

    public void logout(String rawToken) {
        String hash = sha256Hex(rawToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
