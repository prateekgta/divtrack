package io.divtrack.auth.repository;

import io.divtrack.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /** Used at login: look up user by email to verify password. */
    Optional<User> findByEmail(String email);

    /** Used at registration: reject duplicate emails before INSERT. */
    boolean existsByEmail(String email);
}
