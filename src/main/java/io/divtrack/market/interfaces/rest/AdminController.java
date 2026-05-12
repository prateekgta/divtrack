package io.divtrack.market.interfaces.rest;

import io.divtrack.market.domain.service.DataSanityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DataSanityService dataSanityService;

    @GetMapping("/data-health")
    public ResponseEntity<DataSanityService.DataHealthReport> getDataHealth() {
        return ResponseEntity.ok(dataSanityService.generateReport());
    }
}
