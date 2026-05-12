package io.divtrack.subscription.infrastructure.persistence;

import io.divtrack.subscription.domain.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataUserSubscriptionRepo extends JpaRepository<UserSubscription, String> {
    Optional<UserSubscription> findByUserId(String userId);
    Optional<UserSubscription> findByStripeSubscriptionId(String stripeSubscriptionId);
}
