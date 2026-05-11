package io.divtrack.identity.domain.port;

import io.divtrack.identity.domain.model.RefreshToken;
import io.divtrack.identity.domain.model.User;

import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByTokenHash(String hash);
    RefreshToken save(RefreshToken token);
    void revokeAllForUser(User user);
}
