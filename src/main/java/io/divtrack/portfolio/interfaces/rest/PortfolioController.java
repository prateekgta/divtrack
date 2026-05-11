package io.divtrack.portfolio.interfaces.rest;

import io.divtrack.portfolio.application.dto.*;
import io.divtrack.portfolio.application.service.PortfolioApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioApplicationService appService;

    @GetMapping
    public ResponseEntity<PortfolioSummary> getPortfolio(Authentication auth) {
        return ResponseEntity.ok(appService.getPortfolio(auth.getName()));
    }

    @PostMapping("/holdings")
    public ResponseEntity<HoldingDto> addHolding(Authentication auth,
                                                  @Valid @RequestBody AddHoldingRequest req) {
        String plan = extractPlan(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appService.addHolding(auth.getName(), plan, req));
    }

    @DeleteMapping("/holdings/{id}")
    public ResponseEntity<Void> removeHolding(Authentication auth, @PathVariable String id) {
        appService.removeHolding(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertDto>> getAlerts(Authentication auth) {
        return ResponseEntity.ok(appService.getAlerts(auth.getName()));
    }

    @PostMapping("/alerts")
    public ResponseEntity<AlertDto> createAlert(Authentication auth,
                                                  @Valid @RequestBody CreateAlertRequest req) {
        String plan = extractPlan(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appService.createAlert(auth.getName(), plan, req));
    }

    @DeleteMapping("/alerts/{id}")
    public ResponseEntity<Void> deleteAlert(Authentication auth, @PathVariable String id) {
        appService.deleteAlert(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    private String extractPlan(Authentication auth) {
        return auth.getAuthorities().stream()
                .filter(a -> a.getAuthority().startsWith("PLAN_"))
                .findFirst()
                .map(a -> a.getAuthority().replace("PLAN_", ""))
                .orElse("FREE");
    }
}
