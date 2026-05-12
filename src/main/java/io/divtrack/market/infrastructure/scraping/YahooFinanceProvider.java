package io.divtrack.market.infrastructure.scraping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.MarketDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.market.provider", havingValue = "yahoo", matchIfMissing = true)
@RequiredArgsConstructor
public class YahooFinanceProvider implements MarketDataProvider {

    private static final String CHART_URL = "https://query1.finance.yahoo.com/v8/finance/chart/%s?range=1d&interval=1d";

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();

    @Override
    public Map<String, PriceData> fetchPrices(List<Stock> stocks) {
        Map<String, PriceData> results = new ConcurrentHashMap<>();

        CompletableFuture<?>[] futures = stocks.stream()
                .map(stock -> fetchStockPrice(stock)
                        .thenAccept(data -> {
                            if (data != null) {
                                results.put(stock.getTicker().toUpperCase(), data);
                            }
                        })
                        .exceptionally(e -> {
                            log.debug("Failed to fetch {}: {}", stock.getTicker(), e.getMessage());
                            return null;
                        }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        return results;
    }

    private CompletableFuture<PriceData> fetchStockPrice(Stock stock) {
        String url = String.format(CHART_URL, stock.getTicker());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> parsePriceData(response.body(), stock));
    }

    private PriceData parsePriceData(String body, Stock stock) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode meta = root.path("chart").path("result").get(0).path("meta");

            BigDecimal price = parseBigDecimal(meta.path("regularMarketPrice").asText(null));
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) return null;

            BigDecimal yield = parseBigDecimal(meta.path("regularMarketYield").asText(null));
            if (yield == null) {
                yield = parseBigDecimal(meta.path("yield").asText(null));
            }
            if (yield == null) {
                yield = stock.getYieldPct();
            }

            return new PriceData(stock.getTicker(), price, yield);
        } catch (Exception e) {
            log.debug("Failed to parse price for {}: {}", stock.getTicker(), e.getMessage());
            return null;
        }
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
