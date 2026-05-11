CREATE TABLE users (
  id             VARCHAR(26) PRIMARY KEY,
  email          VARCHAR(255) UNIQUE NOT NULL,
  password_hash  VARCHAR(60)  NOT NULL,
  name           VARCHAR(100) NOT NULL,
  plan           VARCHAR(10)  DEFAULT 'free',
  email_verified BOOLEAN      DEFAULT false,
  created_at     TIMESTAMPTZ  DEFAULT NOW(),
  updated_at     TIMESTAMPTZ  DEFAULT NOW()
);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE refresh_tokens (
  id          VARCHAR(26) PRIMARY KEY,
  user_id     VARCHAR(26)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash  VARCHAR(64)  UNIQUE NOT NULL,
  device_info VARCHAR(255),
  is_revoked  BOOLEAN      DEFAULT false,
  expires_at  TIMESTAMPTZ  NOT NULL,
  created_at  TIMESTAMPTZ  DEFAULT NOW()
);
CREATE INDEX idx_rt_user_id  ON refresh_tokens(user_id);
CREATE INDEX idx_rt_expires  ON refresh_tokens(expires_at);
