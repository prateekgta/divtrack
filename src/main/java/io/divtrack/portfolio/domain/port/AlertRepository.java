package io.divtrack.portfolio.domain.port;

import io.divtrack.portfolio.domain.model.Alert;

import java.util.List;
import java.util.Optional;

public interface AlertRepository {
    List<Alert> findByUserId(String userId);
    Optional<Alert> findById(String id);
    Alert save(Alert alert);
    void delete(Alert alert);
    long countByUserId(String userId);
}
