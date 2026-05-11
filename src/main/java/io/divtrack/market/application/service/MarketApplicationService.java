package io.divtrack.market.application.service;

import io.divtrack.market.application.dto.StockDto;
import io.divtrack.market.domain.port.StockRepository;
import io.divtrack.market.domain.service.PriceUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketApplicationService {

    private final StockRepository stockRepository;
    private final PriceUpdateService priceUpdateService;

    public List<StockDto> getAllStocks() {
        return priceUpdateService.getCachedStocks().stream()
                .map(StockDto::from)
                .toList();
    }

    public StockDto getStock(String ticker) {
        return stockRepository.findByTicker(ticker.toUpperCase())
                .map(StockDto::from)
                .orElse(null);
    }

    public List<StockDto> searchStocks(String query) {
        return stockRepository.findAll().stream()
                .filter(s -> s.getTicker().toLowerCase().contains(query.toLowerCase())
                        || (s.getName() != null && s.getName().toLowerCase().contains(query.toLowerCase())))
                .map(StockDto::from)
                .toList();
    }
}
