package io.divtrack.auth.security;

import io.divtrack.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

/**
 * RS256 JWT provider — signs with private key, verifies with public key.
 *
 * WHY RS256 (asymmetric) instead of HS256 (symmetric)?
 * ──────────────────────────────────────────────────────
 * HS256: ONE secret key signs AND verifies.
 *        → Every service (portfolio, market-data, gateway) needs the secret.
 *        → If ANY service is compromised, the attacker can forge tokens.
 *
 * RS256: Private key signs (only auth-service has it).
 *        Public key verifies (every service can have it — it's public).
 *        → Even if portfolio-service is hacked, attacker can't forge tokens.
 *        → Auth-service is the ONLY signing authority.
 *
 * KEY FORMAT EXPECTED IN ENV VARS:
 * ───────────────────────────────────
 * JWT_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----\nMIIEvg...\n-----END PRIVATE KEY-----
 * JWT_PUBLIC_KEY=-----BEGIN PUBLIC KEY-----\nMIIBIj...\n-----END PUBLIC KEY-----
 *
 * The \n in env vars gets converted to real newlines in parsePem().
 * Generate keys with:
 *   openssl genrsa -out private.pem 2048
 *   openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in private.pem -out private_pkcs8.pem
 *   openssl rsa -pubout -in private.pem -out public.pem
 *
 * JWT PAYLOAD STRUCTURE:
 * ──────────────────────
 * {
 *   "sub": "01J9TCRZ3B...",  ← ULID user ID
 *   "email": "alice@...",
 *   "name": "Alice",
 *   "plan": "free",
 *   "iat": 1700000000,
 *   "exp": 1700000900      ← iat + 15 minutes
 * }
 *
 * Downstream services extract user ID from "sub" without a DB call.
 * Plan is in the token so portfolio-service can gate features without
 * calling auth-service on every request.
 */
@Slf4j
@Component
public class JwtProvider {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey  publicKey;
    private final long          expiryMinutes;

    public JwtProvider(
            @Value("${jwt.private-key}") String privateKeyPem,
            @Value("${jwt.public-key}")  String publicKeyPem,
            @Value("${jwt.expiry-minutes:15}") long expiryMinutes
    ) {
        this.expiryMinutes = expiryMinutes;
        try {
            this.privateKey = parsePrivateKey(privateKeyPem);
            this.publicKey  = parsePublicKey(publicKeyPem);
            log.info("[JWT] RS256 keys loaded — expiry={}min", expiryMinutes);
        } catch (Exception e) {
            // Fail fast at startup if keys are bad — don't run with broken auth
            throw new IllegalStateException("Failed to load JWT RSA keys. " +
                    "Make sure JWT_PRIVATE_KEY and JWT_PUBLIC_KEY are set correctly.", e);
        }
    }

    // ── Token generation ─────────────────────────────────────────────────────

    /**
     * Create a signed access token for this user.
     * Called after successful login or register.
     */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("name",  user.getName())
                .claim("plan",  user.getPlan())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiryMinutes, ChronoUnit.MINUTES)))
                .signWith(privateKey)           // jjwt infers RS256 from RSAPrivateKey
                .compact();
    }

    /** How many seconds until the next access token expires (for AuthResponse.expiresIn). */
    public long expirySeconds() {
        return expiryMinutes * 60;
    }

    // ── Token verification ────────────────────────────────────────────────────

    /**
     * Validate signature + expiry, return claims.
     * Throws JwtException (or subclass) if token is tampered, expired, or malformed.
     *
     * Called by the Gateway (which has only the public key) to verify every
     * inbound request before proxying to backend services.
     */
    public Claims validateAndExtract(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validate without throwing — returns null if invalid.
     * Useful when you want to handle bad tokens gracefully (e.g., silent refresh).
     */
    public Claims tryValidate(String token) {
        try {
            return validateAndExtract(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("[JWT] Token validation failed: {}", e.getMessage());
            return null;
        }
    }

    // ── PEM key parsing ───────────────────────────────────────────────────────

    /**
     * Parse PKCS#8 PEM private key.
     * Handles both real newlines and \n-escaped strings (common in env vars).
     *
     * PKCS#8 = "BEGIN PRIVATE KEY" (unencrypted, no passphrase).
     * This is the output of: openssl pkcs8 -topk8 -nocrypt ...
     */
    private RSAPrivateKey parsePrivateKey(String pem) throws Exception {
        byte[] decoded = decodePem(pem, "PRIVATE KEY");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    /**
     * Parse X.509 SubjectPublicKeyInfo PEM public key.
     * This is the output of: openssl rsa -pubout ...
     */
    private RSAPublicKey parsePublicKey(String pem) throws Exception {
        byte[] decoded = decodePem(pem, "PUBLIC KEY");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    /**
     * Strip PEM headers and Base64-decode the key material.
     *
     * Handles env-var format where newlines are encoded as literal \n:
     *   "-----BEGIN PRIVATE KEY-----\nMIIEvg...\n-----END PRIVATE KEY-----"
     * And standard multi-line PEM format.
     */
    private byte[] decodePem(String pem, String keyType) {
        String cleaned = pem
                .replace("\\n", "\n")           // handle env-var escaped newlines
                .replace("-----BEGIN " + keyType + "-----", "")
                .replace("-----END "   + keyType + "-----", "")
                .replaceAll("\\s+", "");         // remove all whitespace (newlines, spaces)
        return Base64.getDecoder().decode(cleaned);
    }
}
