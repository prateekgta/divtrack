package io.divtrack.market.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    @Test
    void constructorSetsFields() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        assertEquals("AAPL", s.getTicker());
        assertEquals("Apple Inc.", s.getName());
        assertEquals("Technology", s.getSector());
    }

    @Test
    void constructorInitializesDefaults() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        assertEquals(BigDecimal.ZERO, s.getPrice());
        assertEquals(BigDecimal.ZERO, s.getYieldPct());
        assertEquals("MONTHLY", s.getDividendFrequency());
        assertEquals(BigDecimal.ZERO, s.getPreviousClose());
        assertEquals(BigDecimal.ZERO, s.getChangePct());
        assertEquals("US", s.getCountry());
        assertEquals("income", s.getCategory());
        assertFalse(s.isNonCumulative());
    }

    @Test
    void updatePriceSetsPreviousCloseFromOldPrice() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.updatePrice(BigDecimal.valueOf(150), BigDecimal.valueOf(1.5));
        assertEquals(BigDecimal.ZERO, s.getPreviousClose());
        assertEquals(BigDecimal.valueOf(150), s.getPrice());
        assertEquals(new BigDecimal("1.5"), s.getYieldPct());
    }

    @Test
    void updatePriceCalculatesPositiveChangePct() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.updatePrice(BigDecimal.valueOf(100), BigDecimal.ZERO);
        s.updatePrice(BigDecimal.valueOf(110), BigDecimal.ZERO);
        assertEquals(BigDecimal.valueOf(100), s.getPreviousClose());
        assertEquals(new BigDecimal("10.0000"), s.getChangePct());
    }

    @Test
    void updatePriceCalculatesNegativeChangePct() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.updatePrice(BigDecimal.valueOf(100), BigDecimal.ZERO);
        s.updatePrice(BigDecimal.valueOf(90), BigDecimal.ZERO);
        assertEquals(BigDecimal.valueOf(100), s.getPreviousClose());
        assertEquals(new BigDecimal("-10.0000"), s.getChangePct());
    }

    @Test
    void updatePriceWithPreviousPriceZeroSetsChangePctToZero() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.updatePrice(BigDecimal.valueOf(50), BigDecimal.valueOf(2));
        assertEquals(BigDecimal.ZERO, s.getChangePct());
    }

    @Test
    void updatePriceWithZeroOldPriceDoesNotDivide() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.updatePrice(BigDecimal.valueOf(100), BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, s.getChangePct());
        assertEquals(BigDecimal.ZERO, s.getPreviousClose());
    }

    @Test
    void setAndGetParValue() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.setParValue(BigDecimal.valueOf(25));
        assertEquals(BigDecimal.valueOf(25), s.getParValue());
    }

    @Test
    void setAndGetNonCumulative() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        assertFalse(s.isNonCumulative());
        s.setNonCumulative(true);
        assertTrue(s.isNonCumulative());
    }

    @Test
    void setAndGetTags() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.setTags("blue-chip,tech");
        assertEquals("blue-chip,tech", s.getTags());
    }

    @Test
    void setAndGetCategory() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.setCategory("growth");
        assertEquals("growth", s.getCategory());
    }

    @Test
    void setAndGetCountry() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.setCountry("CA");
        assertEquals("CA", s.getCountry());
    }

    @Test
    void setAndGetDividendFrequency() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.setDividendFrequency("QUARTERLY");
        assertEquals("QUARTERLY", s.getDividendFrequency());
    }

    @Test
    void setAndGetLastPriceUpdate() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        OffsetDateTime now = OffsetDateTime.now();
        s.setLastPriceUpdate(now);
        assertEquals(now, s.getLastPriceUpdate());
    }

    @Test
    void updatePriceSetsYield() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.updatePrice(BigDecimal.valueOf(100), BigDecimal.valueOf(2.5));
        assertEquals(new BigDecimal("2.5"), s.getYieldPct());
    }

    @Test
    void updatePriceFractionalChange() {
        Stock s = new Stock("AAPL", "Apple Inc.", "Technology");
        s.updatePrice(BigDecimal.valueOf(100), BigDecimal.ZERO);
        s.updatePrice(BigDecimal.valueOf(100.50), BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.5000"), s.getChangePct());
    }
}
