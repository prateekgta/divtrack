package io.divtrack.dividend.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record DividendMilestone(
        BigDecimal totalMonthlyIncome,
        BigDecimal totalAnnualIncome,
        BigDecimal monthlyExpenses,
        BigDecimal coverageRatio,
        BigDecimal gapToFreedom,
        List<BillCoverage> billCoverages,
        String nextMilestoneName,
        BigDecimal nextMilestoneThreshold,
        BigDecimal nextMilestoneGap
) {
    public record BillCoverage(
            String billName, BigDecimal billAmount, BigDecimal coveredAmount,
            String coveredByStock, BigDecimal coveragePct
    ) {}
}
