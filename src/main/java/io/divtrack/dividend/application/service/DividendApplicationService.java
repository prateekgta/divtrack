package io.divtrack.dividend.application.service;

import io.divtrack.dividend.application.dto.*;
import io.divtrack.dividend.domain.model.BillMapping;
import io.divtrack.dividend.domain.model.DividendMilestone;
import io.divtrack.dividend.domain.model.SnowballProjection;
import io.divtrack.dividend.domain.model.TaxAdvice;
import io.divtrack.dividend.domain.port.BillMappingRepository;
import io.divtrack.dividend.domain.service.PaycheckService;
import io.divtrack.dividend.domain.service.SnowballService;
import io.divtrack.dividend.domain.service.TaxOptimizerService;
import io.divtrack.market.domain.port.StockRepository;
import io.divtrack.portfolio.domain.service.PlanLimitExceededException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DividendApplicationService {

    private final PaycheckService paycheckService;
    private final SnowballService snowballService;
    private final TaxOptimizerService taxOptimizerService;
    private final BillMappingRepository billMappingRepository;
    private final StockRepository stockRepository;

    public DividendMilestone getPaycheck(String userId) {
        return paycheckService.calculateMilestones(userId);
    }

    public SnowballProjection getSnowball(String userId, String plan, SnowballRequest req) {
        if (!"PRO".equals(plan)) {
            throw new PlanLimitExceededException("Snowball simulator is a Pro feature");
        }
        return snowballService.project(userId, req.monthlyContribution(),
                req.reinvestDividends(), req.projectionYears());
    }

    public TaxAdvice getTaxOptimization(String userId, String plan) {
        if (!"PRO".equals(plan)) {
            throw new PlanLimitExceededException("Tax optimizer is a Pro feature");
        }
        return taxOptimizerService.optimize(userId);
    }

    @Transactional
    public BillMappingDto addBillMapping(String userId, CreateBillMappingRequest req) {
        var stock = stockRepository.findByTicker(req.ticker().toUpperCase())
                .orElseThrow(() -> new PlanLimitExceededException("Stock not found: " + req.ticker()));
        BillMapping bm = new BillMapping(userId, stock.getId(), req.billName(), req.billAmount());
        bm = billMappingRepository.save(bm);
        return BillMappingDto.from(bm, stock.getTicker());
    }
}
