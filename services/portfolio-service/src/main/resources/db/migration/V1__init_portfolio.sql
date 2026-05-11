CREATE TABLE holdings (
  id            VARCHAR(26) PRIMARY KEY,
  user_id       VARCHAR(26)    NOT NULL,
  ticker        VARCHAR(10)    NOT NULL,
  amount_usd    DECIMAL(15,2)  NOT NULL,
  purchase_date DATE,
  notes         TEXT,
  created_at    TIMESTAMPTZ    DEFAULT NOW(),
  updated_at    TIMESTAMPTZ    DEFAULT NOW(),
  UNIQUE(user_id, ticker)
);
CREATE INDEX idx_holdings_user ON holdings(user_id);

CREATE TABLE alerts (
  id             VARCHAR(26) PRIMARY KEY,
  user_id        VARCHAR(26)   NOT NULL,
  ticker         VARCHAR(10)   NOT NULL,
  alert_type     VARCHAR(20)   NOT NULL,
  threshold      DECIMAL(5,2)  NOT NULL,
  is_active      BOOLEAN       DEFAULT true,
  last_triggered TIMESTAMPTZ,
  created_at     TIMESTAMPTZ   DEFAULT NOW()
);
CREATE INDEX idx_alerts_user ON alerts(user_id);
