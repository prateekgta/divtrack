package io.divtrack.subscription.domain.model;

import io.divtrack.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "price_cents", nullable = false)
    private int priceCents;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(nullable = false, length = 10)
    private String interval = "month";

    @Column(name = "stripe_price_id", length = 100)
    private String stripePriceId;

    @Column(name = "max_holdings", nullable = false)
    private int maxHoldings = 10;

    @Column(name = "max_alerts", nullable = false)
    private int maxAlerts = 3;

    @Column(name = "snowball_enabled", nullable = false)
    private boolean snowballEnabled = false;

    @Column(name = "tax_optimizer_enabled", nullable = false)
    private boolean taxOptimizerEnabled = false;

    @Column(nullable = false)
    private boolean featured = false;

    public SubscriptionPlan() {}

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPriceCents() { return priceCents; }
    public String getCurrency() { return currency; }
    public String getInterval() { return interval; }
    public String getStripePriceId() { return stripePriceId; }
    public int getMaxHoldings() { return maxHoldings; }
    public int getMaxAlerts() { return maxAlerts; }
    public boolean isSnowballEnabled() { return snowballEnabled; }
    public boolean isTaxOptimizerEnabled() { return taxOptimizerEnabled; }
    public boolean isFeatured() { return featured; }
}
