package io.divtrack.subscription.domain.port;

import io.divtrack.subscription.domain.model.UserSubscription;
import java.util.Optional;

public interface UserSubscriptionRepository {
    Optional<UserSubscription> findByUserId(String userId);
    Optional<UserSubscription> findByStripeSubscriptionId(String id);
    UserSubscription save(UserSubscription subscription);
}
