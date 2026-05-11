package io.divtrack.identity.domain.port;

import io.divtrack.identity.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findByResetTokenHash(String resetTokenHash);
    boolean existsByEmail(String email);
    User save(User user);
}
