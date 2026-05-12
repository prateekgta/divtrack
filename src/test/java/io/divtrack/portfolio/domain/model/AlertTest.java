package io.divtrack.portfolio.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AlertTest {

    @Test
    void constructorSetsFields() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.PRICE_ABOVE, BigDecimal.valueOf(150));
        assertEquals("user1", a.getUserId());
        assertEquals("stock1", a.getStockId());
        assertEquals(Alert.AlertType.PRICE_ABOVE, a.getType());
        assertEquals(BigDecimal.valueOf(150), a.getThreshold());
        assertTrue(a.isEnabled());
        assertFalse(a.isTriggered());
    }

    @Test
    void shouldTriggerPriceAboveWhenCurrentAtThreshold() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.PRICE_ABOVE, BigDecimal.valueOf(150));
        assertTrue(a.shouldTrigger(BigDecimal.valueOf(150)));
    }

    @Test
    void shouldTriggerPriceAboveWhenCurrentAboveThreshold() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.PRICE_ABOVE, BigDecimal.valueOf(150));
        assertTrue(a.shouldTrigger(BigDecimal.valueOf(200)));
    }

    @Test
    void shouldTriggerPriceAboveReturnsFalseWhenCurrentBelowThreshold() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.PRICE_ABOVE, BigDecimal.valueOf(150));
        assertFalse(a.shouldTrigger(BigDecimal.valueOf(100)));
    }

    @Test
    void shouldTriggerPriceBelowWhenCurrentAtThreshold() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.PRICE_BELOW, BigDecimal.valueOf(100));
        assertTrue(a.shouldTrigger(BigDecimal.valueOf(100)));
    }

    @Test
    void shouldTriggerPriceBelowWhenCurrentBelowThreshold() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.PRICE_BELOW, BigDecimal.valueOf(100));
        assertTrue(a.shouldTrigger(BigDecimal.valueOf(50)));
    }

    @Test
    void shouldTriggerPriceBelowReturnsFalseWhenCurrentAboveThreshold() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.PRICE_BELOW, BigDecimal.valueOf(100));
        assertFalse(a.shouldTrigger(BigDecimal.valueOf(150)));
    }

    @Test
    void shouldTriggerReturnsFalseWhenAlreadyTriggered() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.PRICE_ABOVE, BigDecimal.valueOf(150));
        a.markTriggered();
        assertFalse(a.shouldTrigger(BigDecimal.valueOf(200)));
        assertTrue(a.isTriggered());
    }

    @Test
    void shouldTriggerReturnsFalseWhenDisabled() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.PRICE_ABOVE, BigDecimal.valueOf(150));
        a.setEnabled(false);
        assertFalse(a.shouldTrigger(BigDecimal.valueOf(200)));
    }

    @Test
    void markTriggeredSetsTriggered() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.PRICE_ABOVE, BigDecimal.valueOf(150));
        assertFalse(a.isTriggered());
        a.markTriggered();
        assertTrue(a.isTriggered());
    }

    @Test
    void markTriggeredIsIdempotent() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.PRICE_ABOVE, BigDecimal.valueOf(150));
        a.markTriggered();
        a.markTriggered();
        assertTrue(a.isTriggered());
    }

    @Test
    void shouldTriggerYieldAbove() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.YIELD_ABOVE, BigDecimal.valueOf(5));
        assertTrue(a.shouldTrigger(BigDecimal.valueOf(5)));
        assertTrue(a.shouldTrigger(BigDecimal.valueOf(6)));
        assertFalse(a.shouldTrigger(BigDecimal.valueOf(4)));
    }

    @Test
    void shouldTriggerYieldBelow() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.YIELD_BELOW, BigDecimal.valueOf(3));
        assertTrue(a.shouldTrigger(BigDecimal.valueOf(3)));
        assertTrue(a.shouldTrigger(BigDecimal.valueOf(2)));
        assertFalse(a.shouldTrigger(BigDecimal.valueOf(4)));
    }

    @Test
    void setEnabled() {
        Alert a = new Alert("user1", "stock1", Alert.AlertType.PRICE_ABOVE, BigDecimal.valueOf(150));
        a.setEnabled(false);
        assertFalse(a.isEnabled());
        a.setEnabled(true);
        assertTrue(a.isEnabled());
    }
}
