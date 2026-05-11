package io.divtrack.portfolio.domain.port;

import io.divtrack.portfolio.domain.model.Holding;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository {
    List<Holding> findByUserId(String userId);
    Optional<Holding> findById(String id);
    Optional<Holding> findByUserIdAndStockId(String userId, String stockId);
    Holding save(Holding holding);
    void delete(Holding holding);
    long countByUserId(String userId);
}
