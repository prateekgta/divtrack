package io.divtrack.market.application.dto;

import java.math.BigDecimal;

public record StockDto(
        String id,
        String ticker,
        String name,
        String sector,
        BigDecimal price,
        BigDecimal yieldPct,
        String dividendFrequency,
        BigDecimal previousClose,
        BigDecimal changePct,
        String country,
        String category,
        BigDecimal parValue,
        boolean nonCumulative,
        String tags
) {
    public static StockDto from(io.divtrack.market.domain.model.Stock stock) {
        return new StockDto(stock.getId(), stock.getTicker(), stock.getName(),
                stock.getSector(), stock.getPrice(), stock.getYieldPct(),
                stock.getDividendFrequency(), stock.getPreviousClose(),
                stock.getChangePct(), stock.getCountry(), stock.getCategory(),
                stock.getParValue(), stock.isNonCumulative(), stock.getTags());
    }
}
