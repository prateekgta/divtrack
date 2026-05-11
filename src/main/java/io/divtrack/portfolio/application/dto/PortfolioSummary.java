package io.divtrack.portfolio.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummary(
        BigDecimal totalValue,
        BigDecimal totalMonthlyIncome,
        BigDecimal totalAnnualIncome,
        BigDecimal avgYieldPct,
        long holdingCount,
        List<HoldingDto> holdings
) {}
