package io.divtrack.auth.model;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * JPA entity for the `auth.users` table.
 *
 * WHY ULID for IDs?
 * ─────────────────
 * UUIDs are random — inserts scatter across the B-tree index, causing page splits
 * and poor performance at scale. ULIDs are time-sortable (millisecond prefix) so
 * inserts land near the end of the index — much friendlier to PostgreSQL.
 *
 * Example:  01J9TCRZ3BPQVKMJ0GTSV8Z00D   (26 chars, URL-safe, monotonic)
 *
 * WHY no @GeneratedValue?
 * ────────────────────────
 * We generate the ULID ourselves in @PrePersist. This lets us know the ID before
 * the INSERT happens — useful for audit logs, event publishing, etc.
 *
 * WHY ddl-auto=validate (not create/update)?
 * ───────────────────────────────────────────
 * Flyway owns schema creation. Hibernate's ddl-auto=validate checks that the
 * JPA model matches the DB schema at startup — catches drift without mutating.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(length = 26, updatable = false, nullable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 'free' or 'pro' — controls feature gating downstream.
     * Checked in portfolio-service via the JWT claim, so no DB call needed per request.
     */
    @Column(length = 10)
    private String plan = "free";

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.id        = UlidCreator.getUlid().toString();
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
