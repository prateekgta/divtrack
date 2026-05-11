package io.divtrack.market.domain.model;

import io.divtrack.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "price_history")
public class PriceHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(precision = 12, scale = 4)
    private BigDecimal open;

    @Column(precision = 12, scale = 4)
    private BigDecimal high;

    @Column(precision = 12, scale = 4)
    private BigDecimal low;

    private Long volume;

    @Column(name = "recorded_at", nullable = false)
    private LocalDate recordedAt;

    public PriceHistory() {}

    public PriceHistory(Stock stock, BigDecimal price, LocalDate recordedAt) {
        this.stock = stock;
        this.price = price;
        this.recordedAt = recordedAt;
    }

    public Stock getStock() { return stock; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getOpen() { return open; }
    public BigDecimal getHigh() { return high; }
    public BigDecimal getLow() { return low; }
    public Long getVolume() { return volume; }
    public LocalDate getRecordedAt() { return recordedAt; }
    public void setOpen(BigDecimal open) { this.open = open; }
    public void setHigh(BigDecimal high) { this.high = high; }
    public void setLow(BigDecimal low) { this.low = low; }
    public void setVolume(Long volume) { this.volume = volume; }
}
