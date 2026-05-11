-- Runs once on first Postgres container start (docker-entrypoint-initdb.d)
-- Creates the three schemas so each Spring Boot service can connect
-- with ?currentSchema=<name> before Flyway runs its migrations.

CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS portfolio;
CREATE SCHEMA IF NOT EXISTS market;

GRANT ALL ON SCHEMA auth      TO divtrack;
GRANT ALL ON SCHEMA portfolio TO divtrack;
GRANT ALL ON SCHEMA market    TO divtrack;
