package io.divtrack.market.domain.port;

import io.divtrack.market.domain.model.Stock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MarketDataProvider {
    Map<String, PriceData> fetchPrices(List<Stock> stocks);
    record PriceData(String ticker, BigDecimal price, BigDecimal yieldPct) {}
}
