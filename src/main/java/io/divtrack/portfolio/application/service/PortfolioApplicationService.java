package io.divtrack.portfolio.application.service;

import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.StockRepository;
import io.divtrack.portfolio.application.dto.*;
import io.divtrack.portfolio.domain.model.AccountType;
import io.divtrack.portfolio.domain.model.Alert;
import io.divtrack.portfolio.domain.model.Alert.AlertType;
import io.divtrack.portfolio.domain.model.Holding;
import io.divtrack.portfolio.domain.port.AlertRepository;
import io.divtrack.portfolio.domain.port.HoldingRepository;
import io.divtrack.portfolio.domain.service.HoldingNotFoundException;
import io.divtrack.portfolio.domain.service.PlanLimitExceededException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioApplicationService {

    private final HoldingRepository holdingRepository;
    private final AlertRepository alertRepository;
    private final StockRepository stockRepository;

    @Value("${app.subscription.free-tier.max-holdings:10}")
    private int maxFreeHoldings;

    @Value("${app.subscription.free-tier.max-alerts:3}")
    private int maxFreeAlerts;

    @Transactional
    public HoldingDto addHolding(String userId, String plan, AddHoldingRequest req) {
        if (!"PRO".equals(plan) && holdingRepository.countByUserId(userId) >= maxFreeHoldings) {
            throw new PlanLimitExceededException("Free plan limited to " + maxFreeHoldings + " holdings");
        }

        Stock stock = stockRepository.findByTicker(req.ticker().toUpperCase())
                .orElseThrow(() -> new HoldingNotFoundException("Stock not found: " + req.ticker()));

        if (holdingRepository.findByUserIdAndStockId(userId, stock.getId()).isPresent()) {
            throw new PlanLimitExceededException("Already holding this stock");
        }

        AccountType accountType = req.accountType() != null
                ? AccountType.valueOf(req.accountType())
                : AccountType.TAXABLE;

        Holding holding = new Holding(userId, stock.getId(), req.shares(), req.costBasis(), accountType);
        holding.setNotes(req.notes());
        holding = holdingRepository.save(holding);

        return toDto(holding, stock);
    }

    public PortfolioSummary getPortfolio(String userId) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);
        List<Stock> stocks = stockRepository.findAll();

        List<HoldingDto> dtos = holdings.stream()
                .map(h -> {
                    Stock s = stocks.stream().filter(st -> st.getId().equals(h.getStockId())).findFirst().orElse(null);
                    return toDto(h, s);
                })
                .toList();

        BigDecimal totalValue = dtos.stream()
                .map(HoldingDto::currentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMonthlyIncome = dtos.stream()
                .map(HoldingDto::monthlyIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAnnualIncome = totalMonthlyIncome.multiply(BigDecimal.valueOf(12));

        BigDecimal avgYield = dtos.stream()
                .map(HoldingDto::yieldPct)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!dtos.isEmpty()) avgYield = avgYield.divide(BigDecimal.valueOf(dtos.size()), 2, RoundingMode.HALF_UP);

        return new PortfolioSummary(totalValue, totalMonthlyIncome, totalAnnualIncome, avgYield, dtos.size(), dtos);
    }

    @Transactional
    public void removeHolding(String userId, String holdingId) {
        Holding h = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new HoldingNotFoundException(holdingId));
        if (!h.getUserId().equals(userId)) {
            throw new HoldingNotFoundException(holdingId);
        }
        holdingRepository.delete(h);
    }

    @Transactional
    public AlertDto createAlert(String userId, String plan, CreateAlertRequest req) {
        if (!"PRO".equals(plan) && alertRepository.countByUserId(userId) >= maxFreeAlerts) {
            throw new PlanLimitExceededException("Free plan limited to " + maxFreeAlerts + " alerts");
        }

        Stock stock = stockRepository.findByTicker(req.ticker().toUpperCase())
                .orElseThrow(() -> new HoldingNotFoundException("Stock not found: " + req.ticker()));

        Alert alert = new Alert(userId, stock.getId(), req.resolvedType(), req.threshold());
        alert = alertRepository.save(alert);

        return new AlertDto(alert.getId(), stock.getTicker(), alert.getType(), alert.getThreshold(),
                alert.isEnabled(), alert.isTriggered(), alert.getCreatedAt());
    }

    public List<AlertDto> getAlerts(String userId) {
        return alertRepository.findByUserId(userId).stream()
                .map(a -> {
                    Stock s = stockRepository.findById(a.getStockId()).orElse(null);
                    return new AlertDto(a.getId(), s != null ? s.getTicker() : "?", a.getType(),
                            a.getThreshold(), a.isEnabled(), a.isTriggered(), a.getCreatedAt());
                })
                .toList();
    }

    @Transactional
    public void deleteAlert(String userId, String alertId) {
        Alert a = alertRepository.findById(alertId)
                .orElseThrow(() -> new HoldingNotFoundException("Alert not found: " + alertId));
        if (!a.getUserId().equals(userId)) {
            throw new HoldingNotFoundException("Alert not found: " + alertId);
        }
        alertRepository.delete(a);
    }

    private HoldingDto toDto(Holding h, Stock s) {
        if (s == null) return null;
        BigDecimal income = h.projectedMonthlyIncome(s.getPrice(), s.getYieldPct(), s.getDividendFrequency());
        return new HoldingDto(
                h.getId(), s.getTicker(), s.getName(), h.getShares(),
                h.getCostBasis(), h.getAccountType().name(),
                s.getPrice(), h.getValue(s.getPrice()), s.getYieldPct(),
                income, s.getDividendFrequency(), h.getNotes(), h.getCreatedAt()
        );
    }
}
