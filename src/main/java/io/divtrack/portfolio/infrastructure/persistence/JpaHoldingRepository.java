package io.divtrack.portfolio.infrastructure.persistence;

import io.divtrack.portfolio.domain.model.Holding;
import io.divtrack.portfolio.domain.port.HoldingRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataHoldingRepository extends JpaRepository<Holding, String> {
    List<Holding> findByUserId(String userId);
    Optional<Holding> findByUserIdAndStockId(String userId, String stockId);
    long countByUserId(String userId);
}

@org.springframework.stereotype.Component
class JpaHoldingRepository implements HoldingRepository {

    private final SpringDataHoldingRepository repo;

    JpaHoldingRepository(SpringDataHoldingRepository repo) { this.repo = repo; }

    @Override
    public List<Holding> findByUserId(String userId) { return repo.findByUserId(userId); }

    @Override
    public Optional<Holding> findById(String id) { return repo.findById(id); }

    @Override
    public Optional<Holding> findByUserIdAndStockId(String userId, String stockId) {
        return repo.findByUserIdAndStockId(userId, stockId);
    }

    @Override
    public Holding save(Holding holding) { return repo.save(holding); }

    @Override
    public void delete(Holding holding) { repo.delete(holding); }

    @Override
    public long countByUserId(String userId) { return repo.countByUserId(userId); }
}
