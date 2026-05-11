package io.divtrack.dividend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateBillMappingRequest(
        @NotBlank String ticker,
        @NotBlank String billName,
        @Positive BigDecimal billAmount
) {}
