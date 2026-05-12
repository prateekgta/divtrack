package io.divtrack.subscription.domain.model;

import io.divtrack.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_subscriptions")
public class UserSubscription extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Column(name = "plan_id", nullable = false, length = 26)
    private String planId;

    @Column(name = "stripe_subscription_id", length = 100)
    private String stripeSubscriptionId;

    @Column(name = "stripe_customer_id", length = 100)
    private String stripeCustomerId;

    @Column(nullable = false, length = 20)
    private String status = "active";

    @Column(name = "current_period_start")
    private OffsetDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private OffsetDateTime currentPeriodEnd;

    @Column(name = "cancel_at_period_end", nullable = false)
    private boolean cancelAtPeriodEnd = false;

    public UserSubscription() {}

    public UserSubscription(String userId, String planId) {
        this.userId = userId;
        this.planId = planId;
    }

    public boolean isActive() {
        return "active".equals(status) || "trialing".equals(status);
    }

    public String getUserId() { return userId; }
    public String getPlanId() { return planId; }
    public String getStripeSubscriptionId() { return stripeSubscriptionId; }
    public void setStripeSubscriptionId(String id) { this.stripeSubscriptionId = id; }
    public String getStripeCustomerId() { return stripeCustomerId; }
    public void setStripeCustomerId(String id) { this.stripeCustomerId = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCurrentPeriodStart() { return currentPeriodStart; }
    public void setCurrentPeriodStart(OffsetDateTime d) { this.currentPeriodStart = d; }
    public OffsetDateTime getCurrentPeriodEnd() { return currentPeriodEnd; }
    public void setCurrentPeriodEnd(OffsetDateTime d) { this.currentPeriodEnd = d; }
    public boolean isCancelAtPeriodEnd() { return cancelAtPeriodEnd; }
    public void setCancelAtPeriodEnd(boolean v) { this.cancelAtPeriodEnd = v; }
}
