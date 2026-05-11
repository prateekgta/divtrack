package io.divtrack.dividend.domain.service;

import io.divtrack.dividend.domain.model.DividendMilestone;
import io.divtrack.dividend.domain.model.DividendMilestone.BillCoverage;
import io.divtrack.dividend.domain.model.BillMapping;
import io.divtrack.dividend.domain.port.BillMappingRepository;
import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.StockRepository;
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
public class PaycheckService {

    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;
    private final BillMappingRepository billMappingRepository;

    public DividendMilestone calculateMilestones(String userId) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);
        List<BillMapping> bills = billMappingRepository.findByUserId(userId);

        BigDecimal totalMonthlyIncome = BigDecimal.ZERO;
        List<BillCoverage> billCoverages = new ArrayList<>();

        for (BillMapping bill : bills) {
            Stock stock = stockRepository.findById(bill.getStockId()).orElse(null);
            if (stock == null) continue;

            Holding holding = holdings.stream()
                    .filter(h -> h.getStockId().equals(bill.getStockId()))
                    .findFirst().orElse(null);

            BigDecimal coveredAmount = holding != null
                    ? holding.projectedMonthlyIncome(stock.getPrice(), stock.getYieldPct(), stock.getDividendFrequency())
                    : BigDecimal.ZERO;

            BigDecimal coveragePct = bill.getBillAmount().compareTo(BigDecimal.ZERO) > 0
                    ? coveredAmount.divide(bill.getBillAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            billCoverages.add(new BillCoverage(
                    bill.getBillName(), bill.getBillAmount(), coveredAmount,
                    stock.getTicker(), coveragePct
            ));
        }

        totalMonthlyIncome = holdings.stream()
                .mapToDouble(h -> {
                    Stock s = stockRepository.findById(h.getStockId()).orElse(null);
                    if (s == null) return 0;
                    return h.projectedMonthlyIncome(s.getPrice(), s.getYieldPct(), s.getDividendFrequency()).doubleValue();
                })
                .mapToObj(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAnnualIncome = totalMonthlyIncome.multiply(BigDecimal.valueOf(12));

        BigDecimal monthlyExpenses = bills.stream()
                .map(BillMapping::getBillAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal coverageRatio = monthlyExpenses.compareTo(BigDecimal.ZERO) > 0
                ? totalMonthlyIncome.divide(monthlyExpenses, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal gapToFreedom = monthlyExpenses.subtract(totalMonthlyIncome);
        if (gapToFreedom.compareTo(BigDecimal.ZERO) < 0) gapToFreedom = BigDecimal.ZERO;

        BigDecimal nextMilestone = BigDecimal.valueOf(25);
        BigDecimal nextThresh = monthlyExpenses.multiply(nextMilestone).divide(BigDecimal.valueOf(100), 0, RoundingMode.UP);

        return new DividendMilestone(
                totalMonthlyIncome, totalAnnualIncome, monthlyExpenses,
                coverageRatio, gapToFreedom, billCoverages,
                nextMilestone + "% freedom",
                nextThresh,
                nextThresh.subtract(totalMonthlyIncome)
        );
    }
}
