package io.divtrack.dividend.application.dto;

import java.math.BigDecimal;

public record SnowballRequest(
        BigDecimal monthlyContribution,
        boolean reinvestDividends,
        int projectionYears
) {}
