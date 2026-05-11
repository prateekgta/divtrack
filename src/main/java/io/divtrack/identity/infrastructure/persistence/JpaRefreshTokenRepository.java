package io.divtrack.identity.infrastructure.persistence;

import io.divtrack.identity.domain.model.RefreshToken;
import io.divtrack.identity.domain.model.User;
import io.divtrack.identity.domain.port.RefreshTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllForUser(User user);
}

@org.springframework.stereotype.Component
class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository repo;

    JpaRefreshTokenRepository(SpringDataRefreshTokenRepository repo) { this.repo = repo; }

    @Override
    public Optional<RefreshToken> findByTokenHash(String hash) { return repo.findByTokenHash(hash); }

    @Override
    public RefreshToken save(RefreshToken token) { return repo.save(token); }

    @Override
    public void revokeAllForUser(User user) { repo.revokeAllForUser(user); }
}
