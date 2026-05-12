package io.divtrack.market.domain.service;

import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSanityService {

    private final StockRepository stockRepository;

    public DataHealthReport generateReport() {
        List<Stock> stocks = stockRepository.findAll();
        List<StalePriceWarning> stale = new ArrayList<>();
        List<ParDeviationWarning> parDevs = new ArrayList<>();
        List<YieldWarning> yieldWarnings = new ArrayList<>();
        List<NoPriceWarning> noPrice = new ArrayList<>();

        OffsetDateTime now = OffsetDateTime.now();
        int total = stocks.size();

        for (Stock s : stocks) {
            if (s.getPrice() == null || s.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                noPrice.add(new NoPriceWarning(s.getTicker(), s.getName()));
                continue;
            }

            OffsetDateTime lpu = s.getLastPriceUpdate();
            if (lpu == null) {
                stale.add(new StalePriceWarning(s.getTicker(), s.getName(), "never updated"));
            } else {
                long hours = Duration.between(lpu, now).toHours();
                if (hours > 24) {
                    stale.add(new StalePriceWarning(s.getTicker(), s.getName(),
                            hours + " hours since last update"));
                }
            }

            if (s.getParValue() != null && s.getParValue().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal price = s.getPrice();
                BigDecimal par = s.getParValue();
                BigDecimal deviation = price.subtract(par)
                        .divide(par, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .abs();
                if (deviation.compareTo(BigDecimal.valueOf(50)) > 0) {
                    parDevs.add(new ParDeviationWarning(s.getTicker(), s.getName(),
                            par, price, deviation));
                }
            }

            if (s.getYieldPct() != null && s.getYieldPct().compareTo(BigDecimal.valueOf(20)) > 0) {
                yieldWarnings.add(new YieldWarning(s.getTicker(), s.getName(),
                        s.getYieldPct(), "Yield exceeds 20% - possible data error"));
            }
        }

        DataHealthReport report = new DataHealthReport(total, stale, parDevs, yieldWarnings, noPrice);
        log.info("DataHealthReport: {}/{} ok, {} stale, {} par deviations, {} yield warnings, {} no price",
                total - stale.size() - noPrice.size(), total,
                stale.size(), parDevs.size(), yieldWarnings.size(), noPrice.size());
        return report;
    }

    public record StalePriceWarning(String ticker, String name, String detail) {}
    public record ParDeviationWarning(String ticker, String name, BigDecimal parValue, BigDecimal currentPrice, BigDecimal deviationPct) {}
    public record YieldWarning(String ticker, String name, BigDecimal yieldPct, String detail) {}
    public record NoPriceWarning(String ticker, String name) {}

    public record DataHealthReport(
            int totalStocks,
            List<StalePriceWarning> stalePrices,
            List<ParDeviationWarning> parDeviations,
            List<YieldWarning> yieldWarnings,
            List<NoPriceWarning> noPrice
    ) {
        public int healthyCount() {
            return totalStocks - stalePrices.size() - noPrice.size();
        }

        public boolean isHealthy() {
            return stalePrices.isEmpty() && parDeviations.isEmpty() && yieldWarnings.isEmpty() && noPrice.isEmpty();
        }
    }
}
