package io.divtrack.market.domain.model;

import io.divtrack.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "stocks")
public class Stock extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String ticker;

    private String name;
    private String sector;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "yield_pct", nullable = false, precision = 8, scale = 4)
    private BigDecimal yieldPct = BigDecimal.ZERO;

    @Column(name = "dividend_frequency", nullable = false)
    private String dividendFrequency = "MONTHLY";

    @Column(name = "previous_close", precision = 12, scale = 4)
    private BigDecimal previousClose = BigDecimal.ZERO;

    @Column(name = "change_pct", precision = 8, scale = 4)
    private BigDecimal changePct = BigDecimal.ZERO;

    @Column(length = 50)
    private String country = "US";

    @Column(name = "par_value", precision = 12, scale = 4)
    private java.math.BigDecimal parValue;

    @Column(name = "non_cumulative")
    private boolean nonCumulative = false;

    @Column(length = 200)
    private String tags;

    @Column(name = "last_price_update")
    private java.time.OffsetDateTime lastPriceUpdate;

    @Column(length = 30)
    private String category = "income";

    public Stock() {}

    public Stock(String ticker, String name, String sector) {
        this.ticker = ticker;
        this.name = name;
        this.sector = sector;
    }

    public void updatePrice(BigDecimal newPrice, BigDecimal newYield) {
        this.previousClose = this.price;
        this.price = newPrice;
        this.yieldPct = newYield;
        if (previousClose.compareTo(BigDecimal.ZERO) > 0) {
            this.changePct = newPrice.subtract(previousClose)
                .divide(previousClose, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        } else {
            this.changePct = BigDecimal.ZERO;
        }
        if (changePct.compareTo(BigDecimal.valueOf(50)) > 0
                || changePct.compareTo(BigDecimal.valueOf(-50)) < 0) {
            this.changePct = BigDecimal.ZERO;
        }
    }

    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public String getSector() { return sector; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getYieldPct() { return yieldPct; }
    public String getDividendFrequency() { return dividendFrequency; }
    public void setDividendFrequency(String dividendFrequency) { this.dividendFrequency = dividendFrequency; }
    public BigDecimal getPreviousClose() { return previousClose; }
    public BigDecimal getChangePct() { return changePct; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public java.math.BigDecimal getParValue() { return parValue; }
    public void setParValue(java.math.BigDecimal parValue) { this.parValue = parValue; }
    public boolean isNonCumulative() { return nonCumulative; }
    public void setNonCumulative(boolean nonCumulative) { this.nonCumulative = nonCumulative; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public java.time.OffsetDateTime getLastPriceUpdate() { return lastPriceUpdate; }
    public void setLastPriceUpdate(java.time.OffsetDateTime lastPriceUpdate) { this.lastPriceUpdate = lastPriceUpdate; }
}
