package io.divtrack.market.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record InvestmentSimResponse(
        String ticker,
        String name,
        BigDecimal invested,
        BigDecimal currentValue,
        BigDecimal totalReturn,
        BigDecimal totalReturnPct,
        BigDecimal estimatedDividends,
        List<Milestone> milestones
) {
    public record Milestone(String date, BigDecimal value, BigDecimal invested) {}
}
