package io.divtrack.portfolio.application.dto;

import io.divtrack.portfolio.domain.model.Alert.AlertType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AlertDto(
        String id,
        String ticker,
        AlertType type,
        BigDecimal threshold,
        boolean enabled,
        boolean triggered,
        OffsetDateTime createdAt
) {}
