package io.divtrack.market.infrastructure.persistence;

import io.divtrack.market.domain.model.PriceHistory;
import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.PriceHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface SpringDataPriceHistoryRepo extends JpaRepository<PriceHistory, String> {
    Optional<PriceHistory> findByStockAndRecordedAt(Stock stock, LocalDate date);
    List<PriceHistory> findByStockOrderByRecordedAtAsc(Stock stock);
    List<PriceHistory> findByStockAndRecordedAtAfterOrderByRecordedAtAsc(Stock stock, LocalDate since);
    long countByStock(Stock stock);
}

@org.springframework.stereotype.Component
class JpaPriceHistoryRepository implements PriceHistoryRepository {

    private final SpringDataPriceHistoryRepo repo;

    JpaPriceHistoryRepository(SpringDataPriceHistoryRepo repo) { this.repo = repo; }

    @Override
    public Optional<PriceHistory> findByStockAndRecordedAt(Stock stock, LocalDate date) {
        return repo.findByStockAndRecordedAt(stock, date);
    }

    @Override
    public List<PriceHistory> findByStockOrderByRecordedAtAsc(Stock stock) {
        return repo.findByStockOrderByRecordedAtAsc(stock);
    }

    @Override
    public List<PriceHistory> findByStockAndRecordedAtAfterOrderByRecordedAtAsc(Stock stock, LocalDate since) {
        return repo.findByStockAndRecordedAtAfterOrderByRecordedAtAsc(stock, since);
    }

    @Override
    public void saveAll(List<PriceHistory> records) {
        repo.saveAll(records);
    }

    @Override
    public long countByStock(Stock stock) {
        return repo.countByStock(stock);
    }
}
