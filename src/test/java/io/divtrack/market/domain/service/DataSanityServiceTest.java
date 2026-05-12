package io.divtrack.market.domain.service;

import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DataSanityServiceTest {

    private List<Stock> stockStore;
    private StockRepository repo;
    private DataSanityService service;

    @BeforeEach
    void setUp() {
        stockStore = new ArrayList<>();
        repo = new InMemoryStockRepository(stockStore);
        service = new DataSanityService(repo);
    }

    @Test
    void reportWithHealthyStocks() {
        Stock s = healthyStock("AAPL", "Apple Inc.");
        stockStore.add(s);

        var report = service.generateReport();

        assertEquals(1, report.totalStocks());
        assertTrue(report.stalePrices().isEmpty());
        assertTrue(report.parDeviations().isEmpty());
        assertTrue(report.yieldWarnings().isEmpty());
        assertTrue(report.noPrice().isEmpty());
        assertTrue(report.isHealthy());
    }

    @Test
    void reportWithStalePriceWarning() {
        Stock s = healthyStock("AAPL", "Apple Inc.");
        s.setLastPriceUpdate(OffsetDateTime.now().minusHours(48));
        stockStore.add(s);

        var report = service.generateReport();

        assertEquals(1, report.stalePrices().size());
        assertEquals("AAPL", report.stalePrices().getFirst().ticker());
        assertFalse(report.isHealthy());
    }

    @Test
    void reportWithNoPriceWarning() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.updatePrice(BigDecimal.ZERO, BigDecimal.ZERO);
        stockStore.add(s);

        var report = service.generateReport();

        assertEquals(1, report.noPrice().size());
        assertEquals("AAPL", report.noPrice().getFirst().ticker());
        assertFalse(report.isHealthy());
    }

    @Test
    void reportWithYieldWarning() {
        Stock s = healthyStock("AAPL", "Apple Inc.");
        s.updatePrice(BigDecimal.valueOf(100), BigDecimal.valueOf(30));
        stockStore.add(s);

        var report = service.generateReport();

        assertEquals(1, report.yieldWarnings().size());
        assertEquals("AAPL", report.yieldWarnings().getFirst().ticker());
        assertEquals("Yield exceeds 20% - possible data error", report.yieldWarnings().getFirst().detail());
        assertFalse(report.isHealthy());
    }

    @Test
    void reportWithParDeviationWarning() {
        Stock s = healthyStock("AAPL", "Apple Inc.");
        s.setParValue(BigDecimal.valueOf(100));
        s.updatePrice(BigDecimal.valueOf(200), BigDecimal.valueOf(2));
        stockStore.add(s);

        var report = service.generateReport();

        assertEquals(1, report.parDeviations().size());
        assertEquals("AAPL", report.parDeviations().getFirst().ticker());
        assertTrue(report.parDeviations().getFirst().deviationPct().compareTo(BigDecimal.valueOf(50)) > 0);
        assertFalse(report.isHealthy());
    }

    @Test
    void reportWithMultipleWarnings() {
        Stock s1 = healthyStock("AAPL", "Apple Inc.");
        Stock s2 = healthyStock("GOOGL", "Alphabet Inc.");
        s2.setLastPriceUpdate(OffsetDateTime.now().minusHours(48));
        stockStore.add(s1);
        stockStore.add(s2);

        var report = service.generateReport();

        assertEquals(2, report.totalStocks());
        assertEquals(1, report.stalePrices().size());
        assertEquals(1, report.healthyCount());
    }

    @Test
    void reportWithNoStocksIsHealthy() {
        var report = service.generateReport();

        assertEquals(0, report.totalStocks());
        assertTrue(report.isHealthy());
    }

    @Test
    void reportWithNullLastPriceUpdateMarkedStale() {
        Stock s = healthyStock("AAPL", "Apple Inc.");
        s.setLastPriceUpdate(null);
        stockStore.add(s);

        var report = service.generateReport();

        assertEquals(1, report.stalePrices().size());
        assertEquals("never updated", report.stalePrices().getFirst().detail());
    }

    @Test
    void reportWithParValueNullSkipsParCheck() {
        Stock s = healthyStock("AAPL", "Apple Inc.");
        s.setParValue(null);
        stockStore.add(s);

        var report = service.generateReport();

        assertTrue(report.parDeviations().isEmpty());
    }

    @Test
    void reportWithParValueZeroSkipsParCheck() {
        Stock s = healthyStock("AAPL", "Apple Inc.");
        s.setParValue(BigDecimal.ZERO);
        stockStore.add(s);

        var report = service.generateReport();

        assertTrue(report.parDeviations().isEmpty());
    }

    @Test
    void dataHealthReportIsHealthyWhenAllEmpty() {
        var report = new DataSanityService.DataHealthReport(5, List.of(), List.of(), List.of(), List.of());
        assertTrue(report.isHealthy());
    }

    @Test
    void dataHealthReportIsHealthyReturnsFalseWithStalePrices() {
        var report = new DataSanityService.DataHealthReport(5,
                List.of(new DataSanityService.StalePriceWarning("AAPL", "Apple Inc.", "test")),
                List.of(), List.of(), List.of());
        assertFalse(report.isHealthy());
    }

    @Test
    void dataHealthReportIsHealthyReturnsFalseWithParDeviations() {
        var report = new DataSanityService.DataHealthReport(5, List.of(),
                List.of(new DataSanityService.ParDeviationWarning("AAPL", "Apple Inc.", BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(90))),
                List.of(), List.of());
        assertFalse(report.isHealthy());
    }

    @Test
    void dataHealthReportIsHealthyReturnsFalseWithYieldWarnings() {
        var report = new DataSanityService.DataHealthReport(5, List.of(), List.of(),
                List.of(new DataSanityService.YieldWarning("AAPL", "Apple Inc.", BigDecimal.valueOf(30), "test")),
                List.of());
        assertFalse(report.isHealthy());
    }

    @Test
    void dataHealthReportIsHealthyReturnsFalseWithNoPrice() {
        var report = new DataSanityService.DataHealthReport(5, List.of(), List.of(), List.of(),
                List.of(new DataSanityService.NoPriceWarning("AAPL", "Apple Inc.")));
        assertFalse(report.isHealthy());
    }

    @Test
    void dataHealthReportHealthyCount() {
        var report = new DataSanityService.DataHealthReport(10,
                List.of(new DataSanityService.StalePriceWarning("A", "A", "test"),
                        new DataSanityService.StalePriceWarning("B", "B", "test")),
                List.of(), List.of(),
                List.of(new DataSanityService.NoPriceWarning("C", "C")));
        assertEquals(7, report.healthyCount());
    }

    private static Stock healthyStock(String ticker, String name) {
        Stock s = new Stock(ticker, name, "Technology");
        s.updatePrice(BigDecimal.valueOf(150), BigDecimal.valueOf(2));
        s.setLastPriceUpdate(OffsetDateTime.now().minusHours(1));
        return s;
    }

    private static class InMemoryStockRepository implements StockRepository {
        private final List<Stock> stocks;

        InMemoryStockRepository(List<Stock> stocks) {
            this.stocks = stocks;
        }

        @Override
        public List<Stock> findAll() {
            return stocks;
        }

        @Override
        public Optional<Stock> findById(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Stock> findByTicker(String ticker) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Stock> findByTickerIn(List<String> tickers) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Stock save(Stock stock) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Stock> saveAll(Iterable<Stock> stocks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long count() {
            return stocks.size();
        }
    }
}
