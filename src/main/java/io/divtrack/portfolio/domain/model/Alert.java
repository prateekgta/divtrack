package io.divtrack.portfolio.domain.model;

import io.divtrack.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "alerts")
public class Alert extends BaseEntity {

    public enum AlertType { PRICE_ABOVE, PRICE_BELOW, YIELD_ABOVE, YIELD_BELOW }

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Column(name = "stock_id", nullable = false, length = 26)
    private String stockId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal threshold;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean triggered = false;

    public Alert() {}

    public Alert(String userId, String stockId, AlertType type, BigDecimal threshold) {
        this.userId = userId;
        this.stockId = stockId;
        this.type = type;
        this.threshold = threshold;
    }

    public boolean shouldTrigger(BigDecimal currentValue) {
        if (!enabled || triggered) return false;
        return switch (type) {
            case PRICE_ABOVE, YIELD_ABOVE -> currentValue.compareTo(threshold) >= 0;
            case PRICE_BELOW, YIELD_BELOW -> currentValue.compareTo(threshold) <= 0;
        };
    }

    public void markTriggered() { this.triggered = true; }

    public String getUserId() { return userId; }
    public String getStockId() { return stockId; }
    public AlertType getType() { return type; }
    public BigDecimal getThreshold() { return threshold; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isTriggered() { return triggered; }
}
