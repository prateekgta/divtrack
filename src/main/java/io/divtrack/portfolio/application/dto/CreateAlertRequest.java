package io.divtrack.portfolio.application.dto;

import io.divtrack.portfolio.domain.model.Alert.AlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateAlertRequest(
        @NotBlank String ticker,
        @NotBlank String type,
        @Positive BigDecimal threshold
) {
    public AlertType resolvedType() { return AlertType.valueOf(type); }
}
