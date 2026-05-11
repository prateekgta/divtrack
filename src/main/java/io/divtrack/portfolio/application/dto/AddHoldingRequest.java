package io.divtrack.portfolio.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AddHoldingRequest(
        @NotBlank String ticker,
        @Positive BigDecimal shares,
        BigDecimal costBasis,
        String accountType,
        String notes
) {}
