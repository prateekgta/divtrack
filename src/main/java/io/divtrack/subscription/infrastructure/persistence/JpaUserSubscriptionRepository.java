package io.divtrack.subscription.infrastructure.persistence;

import io.divtrack.subscription.domain.model.UserSubscription;
import io.divtrack.subscription.domain.port.UserSubscriptionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaUserSubscriptionRepository implements UserSubscriptionRepository {

    private final SpringDataUserSubscriptionRepo repo;

    public JpaUserSubscriptionRepository(SpringDataUserSubscriptionRepo repo) { this.repo = repo; }

    @Override
    public Optional<UserSubscription> findByUserId(String userId) { return repo.findByUserId(userId); }

    @Override
    public Optional<UserSubscription> findByStripeSubscriptionId(String id) { return repo.findByStripeSubscriptionId(id); }

    @Override
    public UserSubscription save(UserSubscription subscription) { return repo.save(subscription); }
}
