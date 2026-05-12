package io.divtrack.subscription.domain.port;

import io.divtrack.subscription.domain.model.SubscriptionPlan;
import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository {
    List<SubscriptionPlan> findAll();
    Optional<SubscriptionPlan> findById(String id);
    Optional<SubscriptionPlan> findByName(String name);
}
