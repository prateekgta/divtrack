# DivTrack — Dividend Portfolio Tracker

Track your dividend portfolio, discover stocks, simulate investments, and explore pre-built portfolio templates — all powered by live Yahoo Finance data.

## Features

- **Portfolio Dashboard** — View your holdings, monthly income, and yield at a glance
- **Stock Browser** — Search 155+ stocks across 7 categories with sector/country filters
- **Performance Charts** — Interactive price history (1m/6m/1y/2y/3y) for any stock
- **Investment Simulator** — "What if I invested $X on date Y?" with dividend projections
- **Portfolio Templates** — 6 pre-built portfolios (Conservative Income to Bitcoin Strategy) with live pricing. One-click adoption.
- **Auto Price Refresh** — Background scheduler refreshes all stock prices every 5 minutes
- **Dividend Tracking** — Monthly income harvest, dividend alerts, snowball simulator

## Architecture

```
divtrack/
├── src/main/java/io/divtrack/     # Spring Boot backend (Java 21)
│   ├── common/                     # TestDataSeeder — seeds 155 stocks
│   ├── identity/                   # Auth (JWT-based registration/login)
│   ├── market/                     # Core market domain
│   │   ├── application/
│   │   │   ├── dto/                # StockDto, PerformanceResponse, PortfolioTemplateDto
│   │   │   └── service/            # MarketApplicationService, PerformanceService, PortfolioTemplateService
│   │   ├── domain/
│   │   │   ├── model/              # Stock, PriceHistory entities
│   │   │   ├── port/               # StockRepository, PriceHistoryRepository, MarketDataProvider
│   │   │   └── service/            # PriceUpdateService (scheduled refresh)
│   │   ├── infrastructure/scraping/ # YahooFinanceProvider (Jsoup), YahooChartProvider (HTTP)
│   │   └── interfaces/rest/        # MarketController (all /api/market/* endpoints)
│   ├── portfolio/                  # Portfolio holdings and alerts
│   └── dividend/                   # Paycheck calculator, snowball, tax optimizer
├── frontend/                       # Next.js 14 (React, TypeScript)
│   └── src/
│       ├── app/dashboard/          # Main dashboard page
│       └── lib/                    # API client, StockBrowser, StockDetail, PortfolioTemplates
├── docker-compose.yml              # postgres + backend + frontend
└── Dockerfile                      # Multi-stage Spring Boot build
```

## Quick Start

### Prerequisites
- Docker & Docker Compose
- JWT RSA key pair (for auth tokens)

### Setup

```bash
# 1. Generate JWT keys
openssl genrsa 2048 | tr -d '\n\r' > .env.tmp
echo "JWT_PRIVATE_KEY=$(cat .env.tmp)" > .env
openssl rsa -in .env.tmp -pubout | tr -d '\n\r' > .env.tmp2
echo "JWT_PUBLIC_KEY=$(cat .env.tmp2)" >> .env
rm -f .env.tmp .env.tmp2

# 2. Start everything
docker compose up -d

# 3. Open dashboard
open http://localhost:3001/dashboard
```

Services:
| Service | Port | URL |
|---------|------|-----|
| Frontend | 3001 | http://localhost:3001 |
| Backend API | 8080 | http://localhost:8080 |
| PostgreSQL | 5432 | internal |

## API Reference

All `/api/market/**` endpoints are **public** (no auth required).

### Stocks

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/market/stocks` | All 155 stocks with live prices |
| GET | `/api/market/stocks/{ticker}` | Single stock detail |
| GET | `/api/market/search?q={query}` | Search by ticker or name |
| GET | `/api/market/top-performers?limit=10` | Top gainers by daily change |
| GET | `/api/market/by-category/{category}` | Filter by category |

### Performance & Simulation

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/market/{ticker}/performance?range=1y` | Price history (1m/6m/1y/2y/3y) |
| GET | `/api/market/{ticker}/simulate?invested=10000&buyDate=2024-01-01` | Investment simulator |

### Portfolio Templates

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/market/templates` | All 6 templates with computed yields |
| GET | `/api/market/templates/{id}?budget=50000` | Single template with custom budget |

### Portfolio (requires auth)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/portfolio` | User's holdings and summary |
| POST | `/api/portfolio/holdings` | Add holding |
| DELETE | `/api/portfolio/holdings/{id}` | Remove holding |
| GET/POST/DELETE | `/api/portfolio/alerts` | Price alerts |

## Templates

| Template | Budget | Risk | Focus | Est. Yield |
|----------|--------|------|-------|------------|
| Conservative Income | $10k | Low | Bonds + REITs + Gold | ~4.3% |
| Balanced Growth | $25k | Medium | ETFs + Dividend Growth | ~2.7% |
| Dividend Aristocrat | $25k | Low-Med | KO, JNJ, PG, MCD, O | ~3.3% |
| Aggressive Growth | $50k | High | NVDA, MSFT, MSTR, TSLA | ~0.5% |
| Monthly Paycheck | $50k | Low-Med | O, JEPI, JEPQ, AGNC | ~7.5% |
| Bitcoin Strategy | $25k | Very High | MSTR, IBIT, COIN, MARA | ~0.0% |

Each template adjusts dynamically to any budget — click "Refresh" to recalculate.

## Data Flow

```
Yahoo Finance API
      │
      ├── YahooFinanceProvider (Jsoup scrape, every 5 min)
      │   └── PriceUpdateService.@Scheduled → updates all Stock.price/yieldPct
      │       └── Caffeine cache (90s TTL)
      │
      └── YahooChartProvider (HTTP chart API, on demand)
          └── PerformanceService → fetches 3y OHLCV → stores in price_history table
```

The scheduler runs automatically. No manual intervention needed for price freshness.

## Stock Categories

| Category | Count | Examples |
|----------|-------|---------|
| `high_yield` | 18 | AGNC, NLY, TWO, IVR, NYMT |
| `monthly_income` | 17 | O, JEPI, JEPQ, MAIN, QQQI |
| `dividend_growth` | 23 | KO, PEP, JNJ, ABBV, TXN |
| `core` | 12 | SPY, VOO, QQQ, SCHD, VTI |
| `growth` | 34 | AAPL, NVDA, MSTR, COIN |
| `income` | 21 | BAC, CVX, MO, GLD, TLT |
| `international` | 30 | RELIANCE.NS, TSM, VXUS |

## Deployment

```bash
# Build and start
docker compose up -d --build

# Check logs
docker compose logs -f backend
docker compose logs -f frontend

# Stop
docker compose down

# Reset database (careful — deletes all data)
docker compose down -v && docker compose up -d
```

### Production Considerations

1. **JWT Keys** — Set `JWT_PRIVATE_KEY` and `JWT_PUBLIC_KEY` as environment variables or in `.env`
2. **Scrape Interval** — Configured at 300s (5 min) in `application.yml` to avoid Yahoo rate limits
3. **Database** — PostgreSQL data persists in Docker volume `pgdata`. Use regular backups.
4. **Caching** — Caffeine cache with 90s expiry. Adjust in `application.yml` if needed.
5. **Security** — Market endpoints are public. Portfolio/auth endpoints require JWT.

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.3, Hibernate, Flyway, Caffeine, JJWT
- **Frontend**: Next.js 14, React, TypeScript, Recharts, Axios
- **Database**: PostgreSQL 16
- **Scraping**: Jsoup (prices), Yahoo Finance Chart API (history)
- **Infrastructure**: Docker Compose
