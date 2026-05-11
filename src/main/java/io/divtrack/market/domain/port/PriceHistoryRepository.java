package io.divtrack.market.domain.port;

import io.divtrack.market.domain.model.PriceHistory;
import io.divtrack.market.domain.model.Stock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PriceHistoryRepository {
    Optional<PriceHistory> findByStockAndRecordedAt(Stock stock, LocalDate date);
    List<PriceHistory> findByStockOrderByRecordedAtAsc(Stock stock);
    List<PriceHistory> findByStockAndRecordedAtAfterOrderByRecordedAtAsc(Stock stock, LocalDate since);
    void saveAll(List<PriceHistory> records);
    long countByStock(Stock stock);
}
