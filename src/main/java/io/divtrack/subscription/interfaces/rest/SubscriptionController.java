package io.divtrack.subscription.interfaces.rest;

import io.divtrack.subscription.domain.model.SubscriptionPlan;
import io.divtrack.subscription.domain.model.UserSubscription;
import io.divtrack.subscription.domain.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlan>> getPlans() {
        return ResponseEntity.ok(subscriptionService.getPlans());
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSubscription(Authentication auth) {
        return subscriptionService.getCurrentSubscription(auth.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/create-checkout")
    public ResponseEntity<Map<String, String>> createCheckout(
            Authentication auth,
            @RequestBody Map<String, String> body) {
        String planId = body.get("planId");
        String url = subscriptionService.createCheckoutSession(auth.getName(), planId, auth.getName());
        if (url == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Stripe not configured"));
        }
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/stripe-webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        subscriptionService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok("received");
    }
}
