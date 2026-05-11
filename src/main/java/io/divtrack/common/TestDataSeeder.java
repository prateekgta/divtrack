package io.divtrack.common;

import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TestDataSeeder {

    private final StockRepository stockRepository;

    @PostConstruct
    public void seed() {
        if (stockRepository.count() > 200) return;

        // ── HIGH YIELD (>8%) ──────────────────────────────────────────────
        seed("AGNC", "AGNC Investment", "Real Estate", 9.87, 14.00, "MONTHLY", "US", "high_yield");
        seed("CLM", "Cornerstone Strategic Value Fund", "Fund", 6.10, 17.00, "MONTHLY", "US", "high_yield");
        seed("ORC", "Orchid Island Capital", "Real Estate", 8.40, 16.00, "MONTHLY", "US", "high_yield");
        seed("OXLC", "Oxford Lane Capital", "Fund", 5.20, 14.00, "MONTHLY", "US", "high_yield");
        seed("PDI", "PIMCO Dynamic Income Fund", "Fund", 23.40, 13.00, "MONTHLY", "US", "high_yield");
        seed("PDO", "PIMCO Dynamic Opportunity Fund", "Fund", 13.20, 11.00, "MONTHLY", "US", "high_yield");
        seed("ECC", "Eagle Point Credit", "Fund", 9.80, 13.00, "MONTHLY", "US", "high_yield");
        seed("GOF", "Guggenheim Strategic Opportunities", "Fund", 14.50, 12.00, "MONTHLY", "US", "high_yield");
        seed("ARR", "ARMOUR Residential REIT", "Real Estate", 20.10, 12.50, "MONTHLY", "US", "high_yield");
        seed("TWO", "Two Harbors Investment", "Real Estate", 12.80, 14.20, "QUARTERLY", "US", "high_yield");
        seed("NLY", "Annaly Capital Management", "Real Estate", 18.90, 13.50, "QUARTERLY", "US", "high_yield");
        seed("IVR", "Invesco Mortgage Capital", "Real Estate", 7.20, 16.50, "QUARTERLY", "US", "high_yield");
        seed("DX", "Dynex Capital", "Real Estate", 12.40, 12.80, "QUARTERLY", "US", "high_yield");
        seed("MFA", "MFA Financial", "Real Estate", 10.60, 12.00, "QUARTERLY", "US", "high_yield");
        seed("RITM", "Rithm Capital", "Real Estate", 10.80, 9.20, "QUARTERLY", "US", "high_yield");
        seed("NYMT", "New York Mortgage Trust", "Real Estate", 7.50, 14.20, "QUARTERLY", "US", "high_yield");
        seed("ET", "Energy Transfer", "Energy", 16.20, 8.10, "QUARTERLY", "US", "high_yield");
        seed("EPD", "Enterprise Products Partners", "Energy", 29.80, 7.20, "QUARTERLY", "US", "high_yield");

        // ── MONTHLY INCOME ────────────────────────────────────────────────
        seed("O", "Realty Income", "Real Estate", 58.40, 5.80, "MONTHLY", "US", "monthly_income");
        seed("MAIN", "Main Street Capital", "Financial", 52.30, 6.90, "MONTHLY", "US", "monthly_income");
        seed("JEPI", "JPMorgan Equity Premium Income", "ETF", 57.30, 7.20, "MONTHLY", "US", "monthly_income");
        seed("JEPQ", "JPMorgan Nasdaq Equity Premium Income", "ETF", 55.10, 8.50, "MONTHLY", "US", "monthly_income");
        seed("SPHD", "Invesco S&P 500 High Dividend", "ETF", 45.60, 4.20, "MONTHLY", "US", "monthly_income");
        seed("STAG", "STAG Industrial", "Real Estate", 38.20, 4.50, "MONTHLY", "US", "monthly_income");
        seed("ADC", "Agree Realty", "Real Estate", 68.50, 4.80, "MONTHLY", "US", "monthly_income");
        seed("GAIN", "Gladstone Investment", "Financial", 13.80, 7.50, "MONTHLY", "US", "monthly_income");
        seed("HTGC", "Hercules Capital", "Financial", 19.80, 9.00, "MONTHLY", "US", "monthly_income");
        seed("BBDC", "Barings BDC", "Financial", 10.20, 9.50, "MONTHLY", "US", "monthly_income");
        seed("TSLX", "Sixth Street Specialty Lending", "Financial", 21.60, 8.50, "MONTHLY", "US", "monthly_income");
        seed("LTC", "LTC Properties", "Real Estate", 35.80, 5.80, "MONTHLY", "US", "monthly_income");
        seed("GMRE", "Global Medical REIT", "Real Estate", 9.40, 8.50, "MONTHLY", "US", "monthly_income");
        seed("STWD", "Starwood Property Trust", "Real Estate", 20.40, 9.50, "QUARTERLY", "US", "monthly_income");
        seed("QQQI", "NEOS Nasdaq 100 High Income ETF", "ETF", 51.20, 9.80, "MONTHLY", "US", "monthly_income");
        seed("SPYI", "NEOS S&P 500 High Income ETF", "ETF", 53.80, 8.60, "MONTHLY", "US", "monthly_income");
        seed("PFF", "iShares Preferred & Income Securities ETF", "ETF", 33.40, 5.90, "MONTHLY", "US", "monthly_income");

        // ── DIVIDEND ARISTOCRATS ───────────────────────────────────────────
        seed("KO", "Coca-Cola", "Consumer Defensive", 62.30, 3.20, "QUARTERLY", "US", "dividend_growth");
        seed("PEP", "PepsiCo", "Consumer Defensive", 172.50, 3.10, "QUARTERLY", "US", "dividend_growth");
        seed("PG", "Procter & Gamble", "Consumer Defensive", 168.20, 2.40, "QUARTERLY", "US", "dividend_growth");
        seed("JNJ", "Johnson & Johnson", "Healthcare", 158.40, 3.00, "QUARTERLY", "US", "dividend_growth");
        seed("ABBV", "AbbVie", "Healthcare", 178.90, 4.70, "QUARTERLY", "US", "dividend_growth");
        seed("HD", "Home Depot", "Retail", 375.20, 2.50, "QUARTERLY", "US", "dividend_growth");
        seed("LOW", "Lowe's", "Retail", 250.80, 1.90, "QUARTERLY", "US", "dividend_growth");
        seed("MCD", "McDonald's", "Restaurant", 285.40, 2.50, "QUARTERLY", "US", "dividend_growth");
        seed("CAT", "Caterpillar", "Industrial", 350.60, 1.60, "QUARTERLY", "US", "dividend_growth");
        seed("WM", "Waste Management", "Industrial", 215.40, 1.40, "QUARTERLY", "US", "dividend_growth");
        seed("TROW", "T. Rowe Price Group", "Financial", 108.20, 4.40, "QUARTERLY", "US", "dividend_growth");
        seed("ADP", "Automatic Data Processing", "Technology", 255.60, 2.10, "QUARTERLY", "US", "dividend_growth");
        seed("CL", "Colgate-Palmolive", "Consumer Defensive", 95.80, 2.30, "QUARTERLY", "US", "dividend_growth");
        seed("KMB", "Kimberly-Clark", "Consumer Defensive", 142.40, 3.40, "QUARTERLY", "US", "dividend_growth");
        seed("MMM", "3M Company", "Industrial", 102.80, 3.20, "QUARTERLY", "US", "dividend_growth");
        seed("AMGN", "Amgen", "Healthcare", 318.50, 3.10, "QUARTERLY", "US", "dividend_growth");
        seed("MDT", "Medtronic", "Healthcare", 85.40, 3.20, "QUARTERLY", "US", "dividend_growth");
        seed("VIG", "Vanguard Dividend Appreciation ETF", "ETF", 195.60, 1.90, "QUARTERLY", "US", "dividend_growth");
        seed("NOBL", "ProShares S&P 500 Dividend Aristocrats ETF", "ETF", 98.40, 2.10, "QUARTERLY", "US", "dividend_growth");
        seed("TXN", "Texas Instruments", "Semiconductors", 175.60, 2.80, "QUARTERLY", "US", "dividend_growth");
        seed("LMT", "Lockheed Martin", "Aerospace", 498.20, 2.50, "QUARTERLY", "US", "dividend_growth");
        seed("HON", "Honeywell International", "Industrial", 212.40, 2.10, "QUARTERLY", "US", "dividend_growth");
        seed("MDLZ", "Mondelez International", "Consumer Defensive", 72.80, 2.60, "QUARTERLY", "US", "dividend_growth");

        // ── CORE HOLDINGS ──────────────────────────────────────────────────
        seed("SCHD", "Schwab US Dividend Equity ETF", "ETF", 78.90, 3.50, "QUARTERLY", "US", "core");
        seed("VOO", "Vanguard S&P 500 ETF", "ETF", 490.20, 1.40, "QUARTERLY", "US", "core");
        seed("VTI", "Vanguard Total Stock Market ETF", "ETF", 260.50, 1.40, "QUARTERLY", "US", "core");
        seed("DGRO", "iShares Core Dividend Growth ETF", "ETF", 57.80, 2.50, "QUARTERLY", "US", "core");
        seed("VYM", "Vanguard High Dividend Yield ETF", "ETF", 121.30, 3.10, "QUARTERLY", "US", "core");
        seed("SPYD", "SPDR Portfolio S&P 500 High Div ETF", "ETF", 42.50, 4.60, "QUARTERLY", "US", "core");
        seed("HDV", "iShares Core High Dividend ETF", "ETF", 112.40, 3.40, "QUARTERLY", "US", "core");
        seed("ARCC", "Ares Capital", "Financial", 21.45, 10.30, "QUARTERLY", "US", "core");
        seed("WPC", "W.P. Carey", "Real Estate", 65.20, 6.20, "QUARTERLY", "US", "core");
        seed("SPY", "SPDR S&P 500 ETF", "ETF", 548.30, 1.30, "QUARTERLY", "US", "core");
        seed("QQQ", "Invesco QQQ Trust", "ETF", 478.60, 0.60, "QUARTERLY", "US", "core");
        seed("IWM", "iShares Russell 2000 ETF", "ETF", 208.40, 1.40, "QUARTERLY", "US", "core");

        // ── GROWTH TECH ────────────────────────────────────────────────────
        seed("AAPL", "Apple", "Technology", 178.50, 0.55, "QUARTERLY", "US", "growth");
        seed("MSFT", "Microsoft", "Technology", 425.30, 0.70, "QUARTERLY", "US", "growth");
        seed("NVDA", "NVIDIA", "Technology", 880.20, 0.04, "QUARTERLY", "US", "growth");
        seed("GOOGL", "Alphabet", "Technology", 175.80, 0.50, "QUARTERLY", "US", "growth");
        seed("META", "Meta Platforms", "Technology", 510.40, 0.40, "QUARTERLY", "US", "growth");
        seed("AVGO", "Broadcom", "Technology", 1350.60, 1.50, "QUARTERLY", "US", "growth");
        seed("AMZN", "Amazon.com", "Technology", 185.40, 0.00, "QUARTERLY", "US", "growth");
        seed("TSLA", "Tesla", "Automotive", 245.80, 0.00, "QUARTERLY", "US", "growth");
        seed("V", "Visa", "Technology", 280.50, 0.80, "QUARTERLY", "US", "growth");
        seed("MA", "Mastercard", "Technology", 465.20, 0.60, "QUARTERLY", "US", "growth");
        seed("COST", "Costco Wholesale", "Retail", 735.80, 0.55, "QUARTERLY", "US", "growth");
        seed("UNH", "UnitedHealth Group", "Healthcare", 528.40, 1.45, "QUARTERLY", "US", "growth");
        seed("CRM", "Salesforce", "Technology", 295.80, 0.40, "QUARTERLY", "US", "growth");
        seed("ADBE", "Adobe", "Technology", 490.30, 0.00, "QUARTERLY", "US", "growth");
        seed("IBM", "IBM", "Technology", 195.20, 3.80, "QUARTERLY", "US", "growth");
        seed("JPM", "JPMorgan Chase", "Financial", 205.40, 2.30, "QUARTERLY", "US", "growth");
        seed("GS", "Goldman Sachs", "Financial", 485.60, 1.50, "QUARTERLY", "US", "growth");
        seed("BLK", "BlackRock", "Financial", 835.20, 2.10, "QUARTERLY", "US", "growth");
        seed("NKE", "Nike", "Consumer Cyclical", 92.40, 1.80, "QUARTERLY", "US", "growth");
        seed("DIS", "Walt Disney", "Entertainment", 112.60, 0.80, "QUARTERLY", "US", "growth");
        seed("SBUX", "Starbucks", "Restaurant", 82.40, 2.60, "QUARTERLY", "US", "growth");
        seed("BRK-B", "Berkshire Hathaway", "Financial", 468.20, 0.00, "QUARTERLY", "US", "growth");
        seed("WMT", "Walmart", "Retail", 82.50, 1.20, "QUARTERLY", "US", "growth");
        seed("GE", "General Electric", "Industrial", 178.40, 0.60, "QUARTERLY", "US", "growth");
        seed("BA", "Boeing", "Aerospace", 185.60, 0.00, "QUARTERLY", "US", "growth");
        seed("TSM", "Taiwan Semiconductor", "Semiconductors", 172.80, 1.30, "QUARTERLY", "US", "growth");
        seed("ASML", "ASML Holding", "Semiconductors", 985.40, 0.80, "QUARTERLY", "US", "growth");
        seed("MSTR", "MicroStrategy", "Technology", 1680.50, 0.00, "QUARTERLY", "US", "growth");
        seed("COIN", "Coinbase Global", "Financial", 245.80, 0.00, "QUARTERLY", "US", "growth");
        seed("MARA", "MARA Holdings", "Technology", 22.40, 0.00, "QUARTERLY", "US", "growth");
        seed("RIOT", "Riot Platforms", "Technology", 11.80, 0.00, "QUARTERLY", "US", "growth");
        seed("CLSK", "CleanSpark", "Technology", 12.60, 0.00, "QUARTERLY", "US", "growth");
        seed("IBIT", "iShares Bitcoin Trust", "ETF", 52.80, 0.00, "QUARTERLY", "US", "growth");
        seed("FBTC", "Fidelity Wise Origin Bitcoin Fund", "ETF", 55.40, 0.00, "QUARTERLY", "US", "growth");

        // ── INCOME ─────────────────────────────────────────────────────────
        seed("PFE", "Pfizer", "Healthcare", 28.50, 5.80, "QUARTERLY", "US", "income");
        seed("BMY", "Bristol-Myers Squibb", "Healthcare", 51.20, 4.50, "QUARTERLY", "US", "income");
        seed("T", "AT&T", "Telecom", 18.40, 5.50, "QUARTERLY", "US", "income");
        seed("VZ", "Verizon", "Telecom", 42.50, 6.50, "QUARTERLY", "US", "income");
        seed("MO", "Altria", "Consumer Defensive", 48.60, 8.50, "QUARTERLY", "US", "income");
        seed("DUK", "Duke Energy", "Utilities", 103.80, 4.20, "QUARTERLY", "US", "income");
        seed("SO", "Southern Company", "Utilities", 80.20, 3.80, "QUARTERLY", "US", "income");
        seed("CVX", "Chevron", "Energy", 155.40, 4.50, "QUARTERLY", "US", "income");
        seed("XOM", "Exxon Mobil", "Energy", 118.60, 3.40, "QUARTERLY", "US", "income");
        seed("SLG", "SL Green Realty", "Real Estate", 55.20, 5.20, "QUARTERLY", "US", "income");
        seed("APO", "Apollo Global Management", "Financial", 118.60, 1.50, "QUARTERLY", "US", "income");
        seed("OKE", "Oneok", "Energy", 86.40, 4.60, "QUARTERLY", "US", "income");
        seed("WMB", "Williams Companies", "Energy", 46.20, 4.80, "QUARTERLY", "US", "income");
        seed("GILD", "Gilead Sciences", "Healthcare", 68.40, 4.20, "QUARTERLY", "US", "income");
        seed("BAC", "Bank of America", "Financial", 42.60, 2.40, "QUARTERLY", "US", "income");
        seed("WFC", "Wells Fargo", "Financial", 68.50, 2.60, "QUARTERLY", "US", "income");
        seed("PLD", "Prologis", "Real Estate", 125.80, 3.20, "QUARTERLY", "US", "income");
        seed("AMT", "American Tower", "Real Estate", 215.40, 3.40, "QUARTERLY", "US", "income");
        seed("GLD", "SPDR Gold Shares", "ETF", 238.50, 0.00, "QUARTERLY", "US", "income");
        seed("TLT", "iShares 20+ Year Treasury Bond ETF", "ETF", 95.80, 4.10, "MONTHLY", "US", "income");
        seed("BND", "Vanguard Total Bond Market ETF", "ETF", 72.40, 3.50, "MONTHLY", "US", "income");

        // ── INTERNATIONAL ──────────────────────────────────────────────────
        seed("RELIANCE.NS", "Reliance Industries", "Energy", 2850.00, 0.40, "QUARTERLY", "India", "international");
        seed("TCS.NS", "Tata Consultancy Services", "Technology", 3850.00, 1.20, "QUARTERLY", "India", "international");
        seed("HDFCBANK.NS", "HDFC Bank", "Financial", 1680.00, 0.90, "QUARTERLY", "India", "international");
        seed("INFY.NS", "Infosys", "Technology", 1450.00, 2.10, "QUARTERLY", "India", "international");
        seed("ITC.NS", "ITC", "Consumer Defensive", 430.00, 2.80, "QUARTERLY", "India", "international");
        seed("ULVR.L", "Unilever", "Consumer Defensive", 42.50, 3.20, "QUARTERLY", "UK", "international");
        seed("BP.L", "BP", "Energy", 5.30, 4.80, "QUARTERLY", "UK", "international");
        seed("GSK.L", "GSK", "Healthcare", 16.80, 3.90, "QUARTERLY", "UK", "international");
        seed("SHEL.L", "Shell", "Energy", 27.40, 3.60, "QUARTERLY", "UK", "international");
        seed("HSBA.L", "HSBC Holdings", "Financial", 6.90, 4.50, "QUARTERLY", "UK", "international");
        seed("SAP.DE", "SAP", "Technology", 185.40, 1.50, "QUARTERLY", "Germany", "international");
        seed("SIEGY", "Siemens", "Industrial", 98.60, 2.80, "QUARTERLY", "Germany", "international");
        seed("ALV.DE", "Allianz", "Financial", 265.30, 3.40, "QUARTERLY", "Germany", "international");
        seed("BAYN.DE", "Bayer", "Healthcare", 28.50, 2.50, "QUARTERLY", "Germany", "international");
        seed("TM", "Toyota Motor", "Automotive", 205.80, 2.60, "QUARTERLY", "Japan", "international");
        seed("SONY", "Sony Group", "Technology", 92.40, 0.80, "QUARTERLY", "Japan", "international");
        seed("MUFG", "Mitsubishi UFJ Financial", "Financial", 10.80, 3.20, "QUARTERLY", "Japan", "international");
        seed("HMC", "Honda Motor", "Automotive", 32.50, 3.00, "QUARTERLY", "Japan", "international");
        seed("MFG", "Mizuho Financial", "Financial", 4.20, 3.50, "QUARTERLY", "Japan", "international");
        seed("SMSN.IL", "Samsung Electronics", "Technology", 1580.00, 1.80, "QUARTERLY", "South Korea", "international");
        seed("HYUP.F", "Hyundai Motor", "Automotive", 210.00, 4.50, "QUARTERLY", "South Korea", "international");
        seed("KEP", "Korea Electric Power", "Utilities", 8.40, 3.60, "QUARTERLY", "South Korea", "international");
        seed("RIO", "Rio Tinto", "Mining", 65.40, 5.20, "SEMI_ANNUAL", "UK", "international");
        seed("BHP", "BHP Group", "Mining", 56.80, 4.80, "SEMI_ANNUAL", "Australia", "international");
        seed("SCCO", "Southern Copper", "Mining", 98.60, 3.40, "QUARTERLY", "Peru", "international");
        seed("BNS", "Bank of Nova Scotia", "Financial", 48.60, 5.80, "QUARTERLY", "Canada", "international");
        seed("RY", "Royal Bank of Canada", "Financial", 102.40, 4.20, "QUARTERLY", "Canada", "international");
        seed("ENB", "Enbridge", "Energy", 38.50, 6.80, "QUARTERLY", "Canada", "international");
        seed("SU", "Suncor Energy", "Energy", 38.20, 4.50, "QUARTERLY", "Canada", "international");
        seed("VXUS", "Vanguard Total International Stock ETF", "ETF", 62.40, 3.10, "QUARTERLY", "US", "international");

        // ── REMAINING (categorized manually above) ─────────────────────────
    }

    private void seed(String ticker, String name, String sector, double price, double yield, String freq, String country, String category) {
        Stock s = stockRepository.findByTicker(ticker).orElseGet(() -> new Stock(ticker, name, sector));
        s.updatePrice(BigDecimal.valueOf(price), BigDecimal.valueOf(yield));
        s.setDividendFrequency(freq);
        s.setCountry(country);
        s.setCategory(category);
        stockRepository.save(s);
    }
}
