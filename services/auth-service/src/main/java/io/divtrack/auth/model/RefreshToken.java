package io.divtrack.auth.model;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
<parameter name="content">package io.divtrack.auth.model;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * JPA entity for the `auth.refresh_tokens` table.
 *
 * SECURITY DESIGN — Why store a hash, not the raw token?
 * ───────────────────────────────────────────────────────
 * We send the raw refresh token to the browser (in an httpOnly cookie).
 * We store SHA-256(raw_token) in the DB.
 *
 * If an attacker reads the DB (SQL injection, backup leak), they get hashes —
 * not the raw tokens — so they can't impersonate users.
 * This is the same principle as storing password hashes, not plaintext.
 *
 * TOKEN ROTATION:
 * ───────────────
 * Every /refresh call issues a NEW refresh token and revokes the old one.
 * If a stolen token is replayed, the legitimate user's next refresh fails
 * (their token is already revoked), alerting us to a potential breach.
 *
 * TOKEN LENGTH:
 * ─────────────
 * 32 random bytes → 256 bits of entropy → impossible to brute-force.
 * Base64URL-encoded to 43 chars (URL-safe, no padding).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @Column(length = 26, updatable = false, nullable = false)
    private String id;

    /**
     * Many tokens can belong to one user (multi-device support).
     * FetchType.LAZY: don't join users table unless we explicitly call getUser().
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * SHA-256 hex of the raw token sent to the browser.
     * LENGTH=64: SHA-256 produces 32 bytes = 64 hex chars.
     */
    @Column(name = "token_hash", unique = true, nullable = false, length = 64)
    private String tokenHash;

    /** Browser/device info from User-Agent header — helps users see active sessions. */
    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "is_revoked")
    private boolean isRevoked = false;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.id        = UlidCreator.getUlid().toString();
        this.createdAt = OffsetDateTime.now();
    }

    /** Convenience: is this token still valid? */
    public boolean isValid() {
        return !isRevoked && expiresAt.isAfter(OffsetDateTime.now());
    }
}
