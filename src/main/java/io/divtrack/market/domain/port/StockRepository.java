package io.divtrack.market.domain.port;

import io.divtrack.market.domain.model.Stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {
    Optional<Stock> findById(String id);
    Optional<Stock> findByTicker(String ticker);
    List<Stock> findAll();
    List<Stock> findByTickerIn(List<String> tickers);
    Stock save(Stock stock);
    List<Stock> saveAll(Iterable<Stock> stocks);
    long count();
}
