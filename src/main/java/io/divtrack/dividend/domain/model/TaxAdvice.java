package io.divtrack.dividend.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record TaxAdvice(
        BigDecimal optimizationScore,
        long suboptimalCount,
        BigDecimal annualTaxSavings,
        List<PlacementAdvice> advices
) {
    public record PlacementAdvice(
            String ticker, BigDecimal currentValue, BigDecimal yieldPct,
            String currentAccount, String recommendedAccount,
            BigDecimal annualTaxImpact, String reason
    ) {}
}
