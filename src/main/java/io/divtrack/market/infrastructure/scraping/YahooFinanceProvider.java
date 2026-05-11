package io.divtrack.market.infrastructure.scraping;

import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.MarketDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.market.provider", havingValue = "yahoo", matchIfMissing = true)
public class YahooFinanceProvider implements MarketDataProvider {

    private static final String URL_TEMPLATE = "https://finance.yahoo.com/quote/%s/";

    @Override
    public Map<String, PriceData> fetchPrices(List<Stock> stocks) {
        Map<String, PriceData> results = new HashMap<>();

        for (Stock stock : stocks) {
            try {
                String url = String.format(URL_TEMPLATE, stock.getTicker());
                Document doc = Jsoup.connect(url)
                        .timeout(10_000)
                        .userAgent("Mozilla/5.0")
                        .get();

                String priceText = doc.select("fin-streamer[data-field='regularMarketPrice']").first() != null
                        ? doc.select("fin-streamer[data-field='regularMarketPrice']").first().attr("value")
                        : null;
                String yieldText = doc.select("td:contains(Dividend Yield) + td").first() != null
                        ? doc.select("td:contains(Dividend Yield) + td").first().text().replace("%", "")
                        : null;

                BigDecimal price = parseBigDecimal(priceText);
                BigDecimal yield = parseBigDecimal(yieldText);

                if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                    results.put(stock.getTicker(), new PriceData(stock.getTicker(), price, yield != null ? yield : stock.getYieldPct()));
                }
            } catch (Exception e) {
                log.debug("Failed to fetch {}: {}", stock.getTicker(), e.getMessage());
            }

            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }

        return results;
    }

    private BigDecimal parseBigDecimal(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return new BigDecimal(text.replace(",", "")).setScale(4, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
