package io.divtrack.dividend.interfaces.rest;

import io.divtrack.dividend.application.dto.*;
import io.divtrack.dividend.application.service.DividendApplicationService;
import io.divtrack.dividend.domain.model.DividendMilestone;
import io.divtrack.dividend.domain.model.SnowballProjection;
import io.divtrack.dividend.domain.model.TaxAdvice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dividend")
@RequiredArgsConstructor
public class DividendController {

    private final DividendApplicationService appService;

    @GetMapping("/paycheck")
    public ResponseEntity<DividendMilestone> getPaycheck(Authentication auth) {
        return ResponseEntity.ok(appService.getPaycheck(auth.getName()));
    }

    @PostMapping("/snowball")
    public ResponseEntity<SnowballProjection> getSnowball(Authentication auth,
                                                           @Valid @RequestBody SnowballRequest req) {
        String plan = extractPlan(auth);
        return ResponseEntity.ok(appService.getSnowball(auth.getName(), plan, req));
    }

    @GetMapping("/tax")
    public ResponseEntity<TaxAdvice> getTaxAdvice(Authentication auth) {
        String plan = extractPlan(auth);
        return ResponseEntity.ok(appService.getTaxOptimization(auth.getName(), plan));
    }

    @PostMapping("/bills")
    public ResponseEntity<BillMappingDto> addBillMapping(Authentication auth,
                                                          @Valid @RequestBody CreateBillMappingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appService.addBillMapping(auth.getName(), req));
    }

    private String extractPlan(Authentication auth) {
        return auth.getAuthorities().stream()
                .filter(a -> a.getAuthority().startsWith("PLAN_"))
                .findFirst()
                .map(a -> a.getAuthority().replace("PLAN_", ""))
                .orElse("FREE");
    }
}
