package io.divtrack.dividend.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record SnowballProjection(
        BigDecimal currentMonthlyIncome,
        BigDecimal currentPortfolioValue,
        BigDecimal monthlyContribution,
        boolean reinvestDividends,
        List<YearProjection> years,
        int targetYear
) {
    public record YearProjection(
            int year, BigDecimal portfolioValue, BigDecimal monthlyIncome,
            BigDecimal annualContributions, BigDecimal annualDividends
    ) {}
}
