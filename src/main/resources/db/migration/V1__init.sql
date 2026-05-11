CREATE TABLE IF NOT EXISTS users (
    id          VARCHAR(26) PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name        VARCHAR(100) NOT NULL,
    plan        VARCHAR(20) NOT NULL DEFAULT 'FREE',
    monthly_expenses DECIMAL(12,2),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          VARCHAR(26) PRIMARY KEY,
    user_id     VARCHAR(26) NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    device_info VARCHAR(255),
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_hash ON refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);

CREATE TABLE IF NOT EXISTS stocks (
    id          VARCHAR(26) PRIMARY KEY,
    ticker      VARCHAR(10) NOT NULL UNIQUE,
    name        VARCHAR(255),
    sector      VARCHAR(100),
    price       DECIMAL(12,4) NOT NULL DEFAULT 0,
    yield_pct   DECIMAL(6,4) NOT NULL DEFAULT 0,
    dividend_frequency VARCHAR(10) NOT NULL DEFAULT 'MONTHLY',
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_stocks_ticker ON stocks(ticker);

CREATE TABLE IF NOT EXISTS price_history (
    id          VARCHAR(26) PRIMARY KEY,
    stock_id    VARCHAR(26) NOT NULL REFERENCES stocks(id),
    price       DECIMAL(12,4) NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_history_stock ON price_history(stock_id, recorded_at DESC);

CREATE TABLE IF NOT EXISTS holdings (
    id          VARCHAR(26) PRIMARY KEY,
    user_id     VARCHAR(26) NOT NULL REFERENCES users(id),
    stock_id    VARCHAR(26) NOT NULL REFERENCES stocks(id),
    shares      DECIMAL(14,6) NOT NULL,
    cost_basis  DECIMAL(12,2),
    account_type VARCHAR(20) NOT NULL DEFAULT 'TAXABLE',
    notes       VARCHAR(500),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_holdings_user ON holdings(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_holdings_user_stock ON holdings(user_id, stock_id);

CREATE TABLE IF NOT EXISTS alerts (
    id          VARCHAR(26) PRIMARY KEY,
    user_id     VARCHAR(26) NOT NULL REFERENCES users(id),
    stock_id    VARCHAR(26) NOT NULL REFERENCES stocks(id),
    type        VARCHAR(20) NOT NULL,
    threshold   DECIMAL(12,4) NOT NULL,
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    triggered   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_alerts_user ON alerts(user_id);

CREATE TABLE IF NOT EXISTS bill_mappings (
    id          VARCHAR(26) PRIMARY KEY,
    user_id     VARCHAR(26) NOT NULL REFERENCES users(id),
    stock_id    VARCHAR(26) NOT NULL REFERENCES stocks(id),
    bill_name   VARCHAR(100) NOT NULL,
    bill_amount DECIMAL(12,2) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_bill_mappings_user ON bill_mappings(user_id);
