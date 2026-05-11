package io.divtrack.portfolio.infrastructure.persistence;

import io.divtrack.portfolio.domain.model.Alert;
import io.divtrack.portfolio.domain.port.AlertRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataAlertRepository extends JpaRepository<Alert, String> {
    List<Alert> findByUserId(String userId);
    long countByUserId(String userId);
}

@org.springframework.stereotype.Component
class JpaAlertRepository implements AlertRepository {

    private final SpringDataAlertRepository repo;

    JpaAlertRepository(SpringDataAlertRepository repo) { this.repo = repo; }

    @Override
    public List<Alert> findByUserId(String userId) { return repo.findByUserId(userId); }

    @Override
    public Optional<Alert> findById(String id) { return repo.findById(id); }

    @Override
    public Alert save(Alert alert) { return repo.save(alert); }

    @Override
    public void delete(Alert alert) { repo.delete(alert); }

    @Override
    public long countByUserId(String userId) { return repo.countByUserId(userId); }
}
