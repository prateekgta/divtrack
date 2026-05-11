package io.divtrack.identity.infrastructure.security;

import io.divtrack.identity.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
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

@Component
public class JwtProvider {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final long expiryMinutes;

    public JwtProvider(
            @Value("${app.jwt.private-key}") String privateKeyPem,
            @Value("${app.jwt.public-key}") String publicKeyPem,
            @Value("${app.jwt.expiry-minutes:15}") long expiryMinutes
    ) {
        this.expiryMinutes = expiryMinutes;
        try {
            this.privateKey = parsePrivateKey(privateKeyPem);
            this.publicKey = parsePublicKey(publicKeyPem);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load JWT RSA keys", e);
        }
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("plan", user.getPlan())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiryMinutes, ChronoUnit.MINUTES)))
                .signWith(privateKey)
                .compact();
    }

    public long expirySeconds() { return expiryMinutes * 60; }

    public Claims validateAndExtract(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims tryValidate(String token) {
        try {
            return validateAndExtract(token);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private RSAPrivateKey parsePrivateKey(String pem) throws Exception {
        byte[] decoded = decodePem(pem, "PRIVATE KEY");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private RSAPublicKey parsePublicKey(String pem) throws Exception {
        byte[] decoded = decodePem(pem, "PUBLIC KEY");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private byte[] decodePem(String pem, String keyType) {
        String cleaned = pem
                .replace("\\n", "\n")
                .replace("-----BEGIN " + keyType + "-----", "")
                .replace("-----END " + keyType + "-----", "")
                .replaceAll("\\s+", "");
        return Base64.getDecoder().decode(cleaned);
    }
}
