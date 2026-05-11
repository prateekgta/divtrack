package io.divtrack.auth.repository;

import io.divtrack.auth.model.RefreshToken;
import io.divtrack.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    /** Called on /refresh: look up by the SHA-256 hash of the token the client sent. */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Logout-all: revoke every active session for this user.
     * @Modifying + @Query avoids loading all tokens into memory.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user = :user AND rt.isRevoked = false")
    void revokeAllForUser(User user);

    /**
     * Scheduled cleanup (optional): delete expired tokens to keep the table lean.
     * Can be called from a @Scheduled job or a Flyway-backed cron.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff")
    void deleteExpiredBefore(OffsetDateTime cutoff);
}
