CREATE TABLE IF NOT EXISTS subscription_plans (
    id          VARCHAR(26) PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    price_cents INTEGER NOT NULL,
    currency    VARCHAR(3) NOT NULL DEFAULT 'USD',
    interval    VARCHAR(10) NOT NULL DEFAULT 'month',
    stripe_price_id VARCHAR(100),
    max_holdings INTEGER NOT NULL DEFAULT 10,
    max_alerts   INTEGER NOT NULL DEFAULT 3,
    snowball_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    tax_optimizer_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    featured    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_subscriptions (
    id              VARCHAR(26) PRIMARY KEY,
    user_id         VARCHAR(26) NOT NULL REFERENCES users(id),
    plan_id         VARCHAR(26) NOT NULL REFERENCES subscription_plans(id),
    stripe_subscription_id VARCHAR(100),
    stripe_customer_id     VARCHAR(100),
    status          VARCHAR(20) NOT NULL DEFAULT 'active',
    current_period_start TIMESTAMP WITH TIME ZONE,
    current_period_end   TIMESTAMP WITH TIME ZONE,
    cancel_at_period_end BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_subscriptions_user ON user_subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_subscriptions_stripe ON user_subscriptions(stripe_subscription_id);

-- Seed default plans
INSERT INTO subscription_plans (id, name, description, price_cents, interval, max_holdings, max_alerts, snowball_enabled, tax_optimizer_enabled, featured)
VALUES
    ('plan_free', 'Free', 'Get started with basic portfolio tracking', 0, 'month', 10, 3, FALSE, FALSE, TRUE),
    ('plan_pro', 'Pro', 'Unlock unlimited holdings, snowball simulator, and tax optimizer', 500, 'month', 999999, 999999, TRUE, TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;
