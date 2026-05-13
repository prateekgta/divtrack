package io.divtrack.market.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.MarketDataProvider;
import io.divtrack.market.domain.port.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceUpdateService {

    private final StockRepository stockRepository;
    private final MarketDataProvider marketDataProvider;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.market.scrape-interval-seconds:60000}")
    @Transactional
    @CacheEvict(value = "stockPrices", allEntries = true)
    public void refreshPrices() {
        List<Stock> stocks = stockRepository.findAll();
        if (stocks.isEmpty()) return;

        Map<String, MarketDataProvider.PriceData> prices = marketDataProvider.fetchPrices(stocks);
        int updated = 0;
        OffsetDateTime now = OffsetDateTime.now();

        for (Stock stock : stocks) {
            MarketDataProvider.PriceData data = prices.get(stock.getTicker().toUpperCase());
            if (data != null) {
                stock.updatePrice(data.price(), data.yieldPct());
                stock.setLastPriceUpdate(now);
                updated++;
    }

    private void broadcastPrices(List<Stock> stocks) {
        for (Stock stock : stocks) {
            try {
                Map<String, Object> msg = new LinkedHashMap<>();
                msg.put("ticker", stock.getTicker());
                msg.put("price", stock.getPrice());
                msg.put("yieldPct", stock.getYieldPct());
                msg.put("changePct", stock.getChangePct());
                msg.put("timestamp", OffsetDateTime.now().toString());
                String json = objectMapper.writeValueAsString(msg);
                redisTemplate.convertAndSend("prices:live", json).subscribe();
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize price update for {}", stock.getTicker());
            }
        }
    }
        }

        stockRepository.saveAll(stocks);
        log.info("Refreshed prices for {}/{} stocks", updated, stocks.size());
        broadcastPrices(stocks);
    }

    @Cacheable("stockPrices")
    public List<Stock> getCachedStocks() {
        return stockRepository.findAll();
    }
}
