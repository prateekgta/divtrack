package io.divtrack.portfolio.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record HoldingDto(
        String id,
        String ticker,
        String stockName,
        BigDecimal shares,
        BigDecimal costBasis,
        String accountType,
        BigDecimal currentPrice,
        BigDecimal currentValue,
        BigDecimal yieldPct,
        BigDecimal monthlyIncome,
        String dividendFrequency,
        String notes,
        OffsetDateTime createdAt
) {}
