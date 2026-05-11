package io.divtrack.market.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record PerformanceResponse(
        String ticker,
        String name,
        List<PricePoint> history,
        BigDecimal totalReturnPct,
        BigDecimal priceReturnPct,
        BigDecimal dividendYield,
        int dataPoints,
        String range
) {
    public record PricePoint(String date, BigDecimal close) {}
}
