package io.divtrack.subscription.domain.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import io.divtrack.subscription.domain.model.SubscriptionPlan;
import io.divtrack.subscription.domain.model.UserSubscription;
import io.divtrack.subscription.domain.port.SubscriptionPlanRepository;
import io.divtrack.subscription.domain.port.UserSubscriptionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository subscriptionRepository;

    @Value("${app.stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${app.stripe.success-url:http://localhost:3000/dashboard}")
    private String successUrl;

    @Value("${app.stripe.cancel-url:http://localhost:3000/pricing}")
    private String cancelUrl;

    @PostConstruct
    void init() {
        if (!stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }

    public List<SubscriptionPlan> getPlans() {
        return planRepository.findAll();
    }

    public Optional<SubscriptionPlan> getDefaultPlan() {
        return planRepository.findByName("Free");
    }

    public Optional<UserSubscription> getCurrentSubscription(String userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    public boolean isPro(String userId) {
        return subscriptionRepository.findByUserId(userId)
                .filter(UserSubscription::isActive)
                .filter(sub -> !"plan_free".equals(sub.getPlanId()))
                .isPresent();
    }

    public String createCheckoutSession(String userId, String planId, String email) {
        if (stripeSecretKey.isBlank()) {
            log.warn("Stripe not configured — cannot create checkout session");
            return null;
        }

        var plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plan: " + planId));

        try {
            var params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setCustomerEmail(email)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(plan.getStripePriceId())
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putMetadata("user_id", userId)
                    .putMetadata("plan_id", planId)
                    .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            log.error("Failed to create Stripe checkout session", e);
            throw new RuntimeException("Failed to create checkout session", e);
        }
    }

    public void handleStripeWebhook(String payload, String sigHeader) {
        log.info("Stripe webhook received (processing not yet implemented)");
    }
}
