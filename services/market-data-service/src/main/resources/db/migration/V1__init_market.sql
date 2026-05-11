CREATE TABLE stocks (
  ticker            VARCHAR(10) PRIMARY KEY,
  name              VARCHAR(100) NOT NULL,
  stock_type        VARCHAR(20)  NOT NULL,
  current_yield     DECIMAL(5,2),
  current_price     DECIMAL(10,2),
  payout_frequency  VARCHAR(10)  DEFAULT 'monthly',
  risk_level        VARCHAR(10)  DEFAULT 'medium',
  last_scraped_at   TIMESTAMPTZ,
  created_at        TIMESTAMPTZ  DEFAULT NOW(),
  updated_at        TIMESTAMPTZ  DEFAULT NOW()
);

CREATE TABLE price_history (
  id          BIGSERIAL PRIMARY KEY,
  ticker      VARCHAR(10)   NOT NULL REFERENCES stocks(ticker),
  recorded_at TIMESTAMPTZ   NOT NULL,
  yield_pct   DECIMAL(5,2),
  price_usd   DECIMAL(10,2)
);
CREATE INDEX idx_ph_ticker_time ON price_history(ticker, recorded_at DESC);
