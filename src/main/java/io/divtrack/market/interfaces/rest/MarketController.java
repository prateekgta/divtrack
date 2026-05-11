package io.divtrack.market.interfaces.rest;

import io.divtrack.market.application.dto.InvestmentSimResponse;
import io.divtrack.market.application.dto.PerformanceResponse;
import io.divtrack.market.application.dto.PortfolioTemplateDto;
import io.divtrack.market.application.dto.StockDto;
import io.divtrack.market.application.service.MarketApplicationService;
import io.divtrack.market.application.service.PerformanceService;
import io.divtrack.market.application.service.PortfolioTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketApplicationService appService;
    private final PerformanceService performanceService;
    private final PortfolioTemplateService templateService;

    @GetMapping("/stocks")
    public ResponseEntity<List<StockDto>> getAllStocks() {
        return ResponseEntity.ok(appService.getAllStocks());
    }

    @GetMapping("/stocks/{ticker}")
    public ResponseEntity<StockDto> getStock(@PathVariable String ticker) {
        StockDto dto = appService.getStock(ticker);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<StockDto>> search(@RequestParam String q) {
        return ResponseEntity.ok(appService.searchStocks(q));
    }

    @GetMapping("/{ticker}/performance")
    public ResponseEntity<PerformanceResponse> getPerformance(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "1y") String range) {
        return ResponseEntity.ok(performanceService.getPerformance(ticker, range));
    }

    @GetMapping("/{ticker}/simulate")
    public ResponseEntity<InvestmentSimResponse> simulate(
            @PathVariable String ticker,
            @RequestParam BigDecimal invested,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate buyDate) {
        return ResponseEntity.ok(performanceService.simulate(ticker, invested, buyDate));
    }

    @GetMapping("/top-performers")
    public ResponseEntity<List<StockDto>> getTopPerformers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(
                performanceService.getTopPerformers(limit).stream()
                        .map(StockDto::from).toList());
    }

    @GetMapping("/by-category/{category}")
    public ResponseEntity<List<StockDto>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(
                performanceService.getByCategory(category).stream()
                        .map(StockDto::from).toList());
    }

    @GetMapping("/templates")
    public ResponseEntity<List<PortfolioTemplateDto>> getTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<PortfolioTemplateDto> getTemplate(
            @PathVariable String id,
            @RequestParam(required = false) Long budget) {
        var result = budget != null
                ? templateService.getTemplateWithBudget(id, budget)
                : templateService.getTemplate(id);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
