package io.divtrack.market.application.service;

import io.divtrack.market.application.dto.InvestmentSimResponse;
import io.divtrack.market.application.dto.PerformanceResponse;
import io.divtrack.market.application.dto.PerformanceResponse.PricePoint;
import io.divtrack.market.domain.model.PriceHistory;
import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.PriceHistoryRepository;
import io.divtrack.market.domain.port.StockRepository;
import io.divtrack.market.infrastructure.scraping.YahooChartProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final StockRepository stockRepository;
    private final PriceHistoryRepository historyRepository;
    private final YahooChartProvider chartProvider;

    public PerformanceResponse getPerformance(String ticker, String range) {
        Stock stock = stockRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Stock not found: " + ticker));

        ensureHistoryLoaded(stock);

        int days = switch (range) {
            case "1m" -> 30;
            case "6m" -> 180;
            case "1y" -> 365;
            case "2y" -> 730;
            case "3y" -> 1095;
            default -> 365;
        };

        LocalDate since = LocalDate.now().minusDays(days);
        List<PriceHistory> records = historyRepository.findByStockAndRecordedAtAfterOrderByRecordedAtAsc(stock, since);

        if (records.isEmpty()) {
            return new PerformanceResponse(ticker, stock.getName(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO, stock.getYieldPct(), 0, range);
        }

        List<PricePoint> points = records.stream()
                .map(r -> new PricePoint(r.getRecordedAt().toString(), r.getPrice()))
                .toList();

        BigDecimal firstPrice = records.getFirst().getPrice();
        BigDecimal lastPrice = records.getLast().getPrice();
        BigDecimal totalReturnPct = firstPrice.compareTo(BigDecimal.ZERO) > 0
                ? lastPrice.subtract(firstPrice).divide(firstPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal priceReturnPct = totalReturnPct;

        return new PerformanceResponse(ticker, stock.getName(), points, totalReturnPct, priceReturnPct, stock.getYieldPct(), points.size(), range);
    }

    public InvestmentSimResponse simulate(String ticker, BigDecimal invested, LocalDate buyDate) {
        Stock stock = stockRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Stock not found: " + ticker));

        ensureHistoryLoaded(stock);

        List<PriceHistory> allHistory = historyRepository.findByStockOrderByRecordedAtAsc(stock);
        if (allHistory.isEmpty()) {
            throw new RuntimeException("No price history for " + ticker);
        }

        PriceHistory buyPoint = null;
        for (PriceHistory ph : allHistory) {
            if (!ph.getRecordedAt().isBefore(buyDate)) {
                buyPoint = ph;
                break;
            }
        }
        if (buyPoint == null) buyPoint = allHistory.getFirst();

        BigDecimal buyPrice = buyPoint.getPrice();
        if (buyPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid buy price for " + ticker);
        }

        BigDecimal shares = invested.divide(buyPrice, 6, RoundingMode.HALF_UP);
        BigDecimal currentValue = shares.multiply(stock.getPrice()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyDivPerShare = stock.getYieldPct()
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .multiply(stock.getPrice())
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        BigDecimal monthsHeld = BigDecimal.valueOf(java.time.temporal.ChronoUnit.MONTHS.between(buyDate.atStartOfDay(), LocalDate.now().atStartOfDay()));
        if (monthsHeld.compareTo(BigDecimal.ZERO) < 0) monthsHeld = BigDecimal.ZERO;
        BigDecimal estimatedDividends = monthlyDivPerShare.multiply(shares).multiply(monthsHeld).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalReturn = currentValue.add(estimatedDividends).subtract(invested);
        BigDecimal totalReturnPct = invested.compareTo(BigDecimal.ZERO) > 0
                ? totalReturn.divide(invested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        List<InvestmentSimResponse.Milestone> milestones = new ArrayList<>();
        for (int i = 0; i < allHistory.size(); i += Math.max(1, allHistory.size() / 10)) {
            PriceHistory ph = allHistory.get(i);
            BigDecimal val = shares.multiply(ph.getPrice()).setScale(2, RoundingMode.HALF_UP);
            milestones.add(new InvestmentSimResponse.Milestone(ph.getRecordedAt().toString(), val, invested));
        }
        PriceHistory last = allHistory.getLast();
        milestones.add(new InvestmentSimResponse.Milestone(last.getRecordedAt().toString(), currentValue, invested));

        return new InvestmentSimResponse(ticker, stock.getName(), invested, currentValue, totalReturn, totalReturnPct, estimatedDividends, milestones);
    }

    public List<Stock> getTopPerformers(int limit) {
        List<Stock> all = stockRepository.findAll();
        return all.stream()
                .filter(s -> s.getCategory() != null && !s.getCategory().equals("international"))
                .sorted((a, b) -> b.getChangePct().compareTo(a.getChangePct()))
                .limit(limit)
                .toList();
    }

    public List<Stock> getByCategory(String category) {
        return stockRepository.findAll().stream()
                .filter(s -> category.equalsIgnoreCase(s.getCategory()))
                .toList();
    }

    private void ensureHistoryLoaded(Stock stock) {
        if (historyRepository.countByStock(stock) > 10) return;
        List<PriceHistory> history = chartProvider.fetchHistory(stock);
        if (!history.isEmpty()) {
            historyRepository.saveAll(history);
            log.info("Loaded {} days of history for {}", history.size(), stock.getTicker());
        }
    }
}
