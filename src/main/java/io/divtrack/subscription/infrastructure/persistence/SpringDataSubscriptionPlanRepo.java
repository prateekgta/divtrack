package io.divtrack.subscription.infrastructure.persistence;

import io.divtrack.subscription.domain.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataSubscriptionPlanRepo extends JpaRepository<SubscriptionPlan, String> {
    Optional<SubscriptionPlan> findByName(String name);
}
