package io.divtrack.market.infrastructure.persistence;

import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.StockRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataStockRepository extends JpaRepository<Stock, String> {
    Optional<Stock> findByTicker(String ticker);
    List<Stock> findByTickerIn(List<String> tickers);
}

@org.springframework.stereotype.Component
class JpaStockRepository implements StockRepository {

    private final SpringDataStockRepository repo;

    JpaStockRepository(SpringDataStockRepository repo) { this.repo = repo; }

    @Override
    public Optional<Stock> findById(String id) { return repo.findById(id); }

    @Override
    public Optional<Stock> findByTicker(String ticker) { return repo.findByTicker(ticker); }

    @Override
    public List<Stock> findAll() { return repo.findAll(); }

    @Override
    public List<Stock> findByTickerIn(List<String> tickers) { return repo.findByTickerIn(tickers); }

    @Override
    public Stock save(Stock stock) { return repo.save(stock); }

    @Override
    public List<Stock> saveAll(Iterable<Stock> stocks) { return repo.saveAll(stocks); }

    @Override
    public long count() { return repo.count(); }
}
