package io.divtrack.identity.infrastructure.persistence;

import io.divtrack.identity.domain.model.User;
import io.divtrack.identity.domain.port.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataUserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

@org.springframework.stereotype.Component
class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository repo;

    JpaUserRepository(SpringDataUserRepository repo) { this.repo = repo; }

    @Override
    public Optional<User> findById(String id) { return repo.findById(id); }

    @Override
    public Optional<User> findByEmail(String email) { return repo.findByEmail(email); }

    @Override
    public boolean existsByEmail(String email) { return repo.existsByEmail(email); }

    @Override
    public User save(User user) { return repo.save(user); }
}
