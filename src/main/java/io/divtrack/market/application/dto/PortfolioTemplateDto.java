package io.divtrack.market.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioTemplateDto(
        String id,
        String name,
        String description,
        String riskLevel,
        String focusArea,
        String budgetLabel,
        BigDecimal defaultBudget,
        int holdingCount,
        BigDecimal estimatedYieldPct,
        BigDecimal estimatedMonthlyIncome,
        List<TemplateAllocationDto> allocations
) {
    public record TemplateAllocationDto(
            String ticker,
            String name,
            BigDecimal allocationPct,
            BigDecimal amount,
            BigDecimal shares,
            BigDecimal price,
            BigDecimal yieldPct,
            String reason
    ) {}
}
