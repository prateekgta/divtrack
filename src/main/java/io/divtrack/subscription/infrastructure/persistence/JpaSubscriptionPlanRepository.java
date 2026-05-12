package io.divtrack.subscription.infrastructure.persistence;

import io.divtrack.subscription.domain.model.SubscriptionPlan;
import io.divtrack.subscription.domain.port.SubscriptionPlanRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaSubscriptionPlanRepository implements SubscriptionPlanRepository {

    private final SpringDataSubscriptionPlanRepo repo;

    public JpaSubscriptionPlanRepository(SpringDataSubscriptionPlanRepo repo) { this.repo = repo; }

    @Override
    public List<SubscriptionPlan> findAll() { return repo.findAll(); }

    @Override
    public Optional<SubscriptionPlan> findById(String id) { return repo.findById(id); }

    @Override
    public Optional<SubscriptionPlan> findByName(String name) { return repo.findByName(name); }
}
