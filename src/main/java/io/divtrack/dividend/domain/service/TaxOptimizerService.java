package io.divtrack.dividend.domain.service;

import io.divtrack.dividend.domain.model.TaxAdvice;
import io.divtrack.dividend.domain.model.TaxAdvice.PlacementAdvice;
import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.StockRepository;
import io.divtrack.portfolio.domain.model.AccountType;
import io.divtrack.portfolio.domain.model.Holding;
import io.divtrack.portfolio.domain.port.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxOptimizerService {

    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;

    public TaxAdvice optimize(String userId) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);

        List<PlacementAdvice> advices = new ArrayList<>();
        int suboptimalCount = 0;
        BigDecimal totalAnnualTaxSavings = BigDecimal.ZERO;

        for (Holding h : holdings) {
            Stock s = stockRepository.findById(h.getStockId()).orElse(null);
            if (s == null) continue;

            AdviceResult advice = evaluatePlacement(h, s);
            if (advice.suboptimal()) {
                suboptimalCount++;
                totalAnnualTaxSavings = totalAnnualTaxSavings.add(advice.annualTaxImpact());
                advices.add(new PlacementAdvice(
                        s.getTicker(),
                        h.getValue(s.getPrice()).setScale(2, RoundingMode.HALF_UP),
                        s.getYieldPct(),
                        h.getAccountType().name(),
                        advice.recommendedAccount(),
                        advice.annualTaxImpact().setScale(2, RoundingMode.HALF_UP),
                        advice.reason()
                ));
            }
        }

        int total = (int) holdings.size();
        BigDecimal score = total > 0
                ? BigDecimal.valueOf(total - suboptimalCount)
                    .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                : BigDecimal.valueOf(100);

        return new TaxAdvice(score.setScale(0, RoundingMode.HALF_UP),
                suboptimalCount, totalAnnualTaxSavings.setScale(2, RoundingMode.HALF_UP), advices);
    }

    private record AdviceResult(boolean suboptimal, String recommendedAccount,
                                BigDecimal annualTaxImpact, String reason) {}

    private AdviceResult evaluatePlacement(Holding h, Stock s) {
        BigDecimal annualIncome = h.getValue(s.getPrice())
                .multiply(s.getYieldPct().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        BigDecimal yield = s.getYieldPct();
        String current = h.getAccountType().name();

        if (yield.compareTo(BigDecimal.valueOf(5)) > 0) {
            if (current.equals("TAXABLE")) {
                BigDecimal taxAt22 = annualIncome.multiply(BigDecimal.valueOf(0.22));
                return new AdviceResult(true, "TRADITIONAL_IRA", taxAt22,
                        "High-yield (" + s.getYieldPct() + "%) in taxable account generates $" + taxAt22.setScale(0, RoundingMode.HALF_UP) + "/yr in taxes. Move to IRA.");
            }
            if (current.equals("ROTH_IRA")) {
                return new AdviceResult(false, "ROTH_IRA", BigDecimal.ZERO, "Perfect placement — tax-free growth.");
            }
        }

        if (yield.compareTo(BigDecimal.valueOf(2)) < 0 && current.equals("ROTH_IRA")) {
            BigDecimal missedSavings = annualIncome.multiply(BigDecimal.valueOf(0.22));
            return new AdviceResult(true, "TAXABLE", missedSavings,
                    "Low-yield stock in Roth IRA. Put high-growth here instead. Move to taxable to free Roth space for high-yield.");
        }

        return new AdviceResult(false, current, BigDecimal.ZERO, "Acceptable placement.");
    }
}
