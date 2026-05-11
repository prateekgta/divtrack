package io.divtrack.dividend.domain.service;

import io.divtrack.dividend.domain.model.SnowballProjection;
import io.divtrack.dividend.domain.model.SnowballProjection.YearProjection;
import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.StockRepository;
import io.divtrack.portfolio.domain.model.Holding;
import io.divtrack.portfolio.domain.port.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SnowballService {

    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;

    public SnowballProjection project(String userId, BigDecimal monthlyContribution,
                                       boolean reinvestDividends, int years) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);

        BigDecimal currentMonthlyIncome = BigDecimal.ZERO;
        BigDecimal currentPortfolioValue = BigDecimal.ZERO;

        for (Holding h : holdings) {
            Stock s = stockRepository.findById(h.getStockId()).orElse(null);
            if (s == null) continue;
            currentPortfolioValue = currentPortfolioValue.add(h.getValue(s.getPrice()));
            currentMonthlyIncome = currentMonthlyIncome.add(
                    h.projectedMonthlyIncome(s.getPrice(), s.getYieldPct(), s.getDividendFrequency()));
        }

        BigDecimal avgYield = calculateAvgWeightedYield(holdings);

        int currentYear = Year.now().getValue();
        List<YearProjection> yearProjections = new ArrayList<>();

        BigDecimal portfolioValue = currentPortfolioValue;
        BigDecimal monthlyIncome = currentMonthlyIncome;

        int targetYearFound = currentYear + years;

        for (int i = 1; i <= years; i++) {
            int year = currentYear + i;

            BigDecimal annualContributions = monthlyContribution.multiply(BigDecimal.valueOf(12));
            BigDecimal annualDividends = monthlyIncome.multiply(BigDecimal.valueOf(12));

            BigDecimal growthFromYield = reinvestDividends
                    ? portfolioValue.multiply(avgYield.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))
                    : annualDividends;

            BigDecimal growthFromPrice = portfolioValue.multiply(BigDecimal.valueOf(0.03));
            BigDecimal totalGrowth = growthFromYield.add(annualContributions).add(growthFromPrice);

            portfolioValue = portfolioValue.add(totalGrowth);
            monthlyIncome = portfolioValue.multiply(avgYield.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

            yearProjections.add(new YearProjection(
                    year, portfolioValue.setScale(0, RoundingMode.HALF_UP),
                    monthlyIncome.setScale(2, RoundingMode.HALF_UP),
                    annualContributions.setScale(0, RoundingMode.HALF_UP),
                    annualDividends.setScale(0, RoundingMode.HALF_UP)));
        }

        return new SnowballProjection(
                currentMonthlyIncome.setScale(2, RoundingMode.HALF_UP),
                currentPortfolioValue.setScale(0, RoundingMode.HALF_UP),
                monthlyContribution, reinvestDividends,
                yearProjections, targetYearFound
        );
    }

    private BigDecimal calculateAvgWeightedYield(List<Holding> holdings) {
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal weightedSum = BigDecimal.ZERO;

        for (Holding h : holdings) {
            Stock s = stockRepository.findById(h.getStockId()).orElse(null);
            if (s == null) continue;
            BigDecimal value = h.getValue(s.getPrice());
            totalValue = totalValue.add(value);
            weightedSum = weightedSum.add(value.multiply(s.getYieldPct()));
        }

        if (totalValue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return weightedSum.divide(totalValue, 4, RoundingMode.HALF_UP);
    }
}
