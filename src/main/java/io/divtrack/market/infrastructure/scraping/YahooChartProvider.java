package io.divtrack.market.infrastructure.scraping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.divtrack.market.domain.model.PriceHistory;
import io.divtrack.market.domain.model.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class YahooChartProvider {

    private static final String CHART_URL = "https://query1.finance.yahoo.com/v8/finance/chart/%s?range=3y&interval=1d";
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<PriceHistory> fetchHistory(Stock stock) {
        List<PriceHistory> records = new ArrayList<>();
        try {
            String url = String.format(CHART_URL, stock.getTicker());
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                log.warn("Yahoo chart returned {} for {}", res.statusCode(), stock.getTicker());
                return records;
            }

            JsonNode root = mapper.readTree(res.body());
            JsonNode result = root.path("chart").path("result").get(0);
            if (result == null) return records;

            JsonNode timestamps = result.path("timestamp");
            JsonNode quotes = result.path("indicators").path("quote").get(0);
            JsonNode adjclose = result.path("indicators").path("adjclose").get(0).path("adjclose");

            if (timestamps == null || quotes == null) return records;

            ZoneId ny = ZoneId.of("America/New_York");

            for (int i = 0; i < timestamps.size(); i++) {
                if (!quotes.has("close") || quotes.get("close").get(i) == null || quotes.get("close").get(i).isNull()) continue;
                BigDecimal close = BigDecimal.valueOf(quotes.get("close").get(i).asDouble());
                LocalDate date = Instant.ofEpochSecond(timestamps.get(i).asLong()).atZone(ny).toLocalDate();
                BigDecimal open = quotes.has("open") && quotes.get("open").get(i) != null && !quotes.get("open").get(i).isNull()
                        ? BigDecimal.valueOf(quotes.get("open").get(i).asDouble()) : null;
                BigDecimal high = quotes.has("high") && quotes.get("high").get(i) != null && !quotes.get("high").get(i).isNull()
                        ? BigDecimal.valueOf(quotes.get("high").get(i).asDouble()) : null;
                BigDecimal low = quotes.has("low") && quotes.get("low").get(i) != null && !quotes.get("low").get(i).isNull()
                        ? BigDecimal.valueOf(quotes.get("low").get(i).asDouble()) : null;
                Long volume = quotes.has("volume") && quotes.get("volume").get(i) != null && !quotes.get("volume").get(i).isNull()
                        ? quotes.get("volume").get(i).asLong() : null;

                PriceHistory ph = new PriceHistory(stock, close, date);
                ph.setOpen(open);
                ph.setHigh(high);
                ph.setLow(low);
                ph.setVolume(volume);
                records.add(ph);
            }

            log.info("Fetched {} days of history for {}", records.size(), stock.getTicker());
        } catch (Exception e) {
            log.debug("Failed to fetch history for {}: {}", stock.getTicker(), e.getMessage());
        }
        return records;
    }

}
