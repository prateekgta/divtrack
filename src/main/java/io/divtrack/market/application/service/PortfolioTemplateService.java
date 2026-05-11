package io.divtrack.market.application.service;

import io.divtrack.market.application.dto.PortfolioTemplateDto;
import io.divtrack.market.application.dto.PortfolioTemplateDto.TemplateAllocationDto;
import io.divtrack.market.domain.model.Stock;
import io.divtrack.market.domain.port.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioTemplateService {

    private final StockRepository stockRepository;

    private record TemplateDef(String id, String name, String description, String riskLevel, String focusArea,
                               String budgetLabel, long defaultBudget, Map<String, TemplateSlot> slots) {}

    private record TemplateSlot(double pct, String reason) {}

    public List<PortfolioTemplateDto> getAllTemplates() {
        List<PortfolioTemplateDto> results = new ArrayList<>();
        for (TemplateDef def : TEMPLATES) {
            buildTemplate(def, def.defaultBudget).ifPresent(results::add);
        }
        return results;
    }

    public Optional<PortfolioTemplateDto> getTemplate(String id) {
        for (TemplateDef def : TEMPLATES) {
            if (def.id().equals(id)) {
                return buildTemplate(def, def.defaultBudget);
            }
        }
        return Optional.empty();
    }

    public Optional<PortfolioTemplateDto> getTemplateWithBudget(String id, long budget) {
        for (TemplateDef def : TEMPLATES) {
            if (def.id().equals(id)) {
                return buildTemplate(def, budget);
            }
        }
        return Optional.empty();
    }

    private Optional<PortfolioTemplateDto> buildTemplate(TemplateDef def, long budget) {
        Map<String, Stock> stocks = loadStocks(def.slots().keySet());
        if (stocks.isEmpty()) return Optional.empty();

        BigDecimal totalBudget = BigDecimal.valueOf(budget);
        List<TemplateAllocationDto> allocations = new ArrayList<>();
        BigDecimal totalYield = BigDecimal.ZERO;

        for (var entry : def.slots().entrySet()) {
            Stock s = stocks.get(entry.getKey());
            if (s == null) continue;

            BigDecimal pct = BigDecimal.valueOf(entry.getValue().pct());
            BigDecimal amount = totalBudget.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal shares = s.getPrice().compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(s.getPrice(), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            allocations.add(new TemplateAllocationDto(
                    s.getTicker(), s.getName(), pct, amount, shares,
                    s.getPrice(), s.getYieldPct(), entry.getValue().reason()));

            totalYield = totalYield.add(s.getYieldPct().multiply(pct));
        }

        BigDecimal avgYield = totalYield.divide(BigDecimal.valueOf(10000), 6, RoundingMode.HALF_UP);
        BigDecimal monthlyIncome = avgYield.multiply(totalBudget)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        return Optional.of(new PortfolioTemplateDto(
                def.id(), def.name(), def.description(), def.riskLevel(), def.focusArea(),
                def.budgetLabel(), totalBudget, allocations.size(), avgYield, monthlyIncome, allocations));
    }

    private Map<String, Stock> loadStocks(Iterable<String> tickers) {
        Map<String, Stock> map = new LinkedHashMap<>();
        for (String t : tickers) {
            stockRepository.findByTicker(t).ifPresent(s -> map.put(t, s));
        }
        return map;
    }

    private static final List<TemplateDef> TEMPLATES = List.of(
            new TemplateDef("conservative-income",
                    "Conservative Income",
                    "Build steady monthly income with bonds, REITs, and high-quality dividend ETFs. Low volatility, predictable returns.",
                    "Low", "Income", "$10k", 10_000,
                    Map.of(
                            "O", new TemplateSlot(25, "Monthly REIT income — 5.8% yield, 600+ consecutive dividends"),
                            "SCHD", new TemplateSlot(20, "Core dividend ETF — 3.5% yield, strong dividend growth"),
                            "BND", new TemplateSlot(20, "Total bond market — 3.5% yield, portfolio stability"),
                            "JEPI", new TemplateSlot(15, "Premium income ETF — 7.2% yield, downside protection"),
                            "GLD", new TemplateSlot(10, "Gold hedge — inflation protection, portfolio diversification"),
                            "TLT", new TemplateSlot(10, "Long-term treasuries — 4.1% yield, recession hedge")
                    )),

            new TemplateDef("balanced-growth",
                    "Balanced Growth",
                    "Mix of broad market ETFs, dividend growers, and income. Designed for long-term wealth building with regular income.",
                    "Medium", "Growth + Income", "$25k", 25_000,
                    Map.of(
                            "VOO", new TemplateSlot(25, "S&P 500 — core US market exposure"),
                            "SCHD", new TemplateSlot(15, "Dividend growth ETF — quality dividend payers"),
                            "QQQ", new TemplateSlot(10, "Nasdaq-100 — tech and innovation leaders"),
                            "O", new TemplateSlot(10, "Realty Income — monthly dividend anchor"),
                            "AAPL", new TemplateSlot(8, "Apple — dominant tech, growing dividend"),
                            "MSFT", new TemplateSlot(7, "Microsoft — AI/cloud leader, consistent payer"),
                            "JEPI", new TemplateSlot(10, "Premium income — enhanced yield"),
                            "BND", new TemplateSlot(10, "Bond buffer — portfolio ballast"),
                            "SPY", new TemplateSlot(5, "Additional S&P 500 exposure")
                    )),

            new TemplateDef("dividend-aristocrat",
                    "Dividend Aristocrat",
                    "Companies with 25+ years of consecutive dividend growth. Focus on quality, dividend safety, and long-term compounding.",
                    "Low-Medium", "Dividend Growth", "$25k", 25_000,
                    Map.of(
                            "KO", new TemplateSlot(12, "Coca-Cola — 62 years of dividend growth"),
                            "JNJ", new TemplateSlot(12, "Johnson & Johnson — 61 years of increases"),
                            "PG", new TemplateSlot(12, "Procter & Gamble — 67 years of dividend growth"),
                            "ABBV", new TemplateSlot(10, "AbbVie — 4.7% yield, strong pharma pipeline"),
                            "HD", new TemplateSlot(10, "Home Depot — retail leader, 14% 5yr div growth"),
                            "MCD", new TemplateSlot(10, "McDonald's — global franchise, 47 years of growth"),
                            "O", new TemplateSlot(10, "Realty Income — monthly aristocrat"),
                            "SCHD", new TemplateSlot(12, "Dividend aristocrat ETF — one-stop shop"),
                            "LMT", new TemplateSlot(6, "Lockheed Martin — defense, 20+ yr div growth"),
                            "TXN", new TemplateSlot(6, "Texas Instruments — 19 years of increases")
                    )),

            new TemplateDef("aggressive-growth",
                    "Aggressive Growth",
                    "High-conviction growth stocks, tech leaders, and Bitcoin strategy. Higher volatility, higher upside. For long-term horizons.",
                    "High", "Growth", "$50k", 50_000,
                    Map.of(
                            "NVDA", new TemplateSlot(15, "NVIDIA — AI compute leader, 880 PE growth"),
                            "MSFT", new TemplateSlot(12, "Microsoft — cloud/AI moat, Azure growth"),
                            "AMZN", new TemplateSlot(10, "Amazon — AWS + e-commerce dominance"),
                            "META", new TemplateSlot(10, "Meta — social media, AI, metaverse bet"),
                            "GOOGL", new TemplateSlot(10, "Alphabet — search + AI + YouTube"),
                            "AVGO", new TemplateSlot(8, "Broadcom — semiconductor + infrastructure"),
                            "MSTR", new TemplateSlot(10, "MicroStrategy — Bitcoin treasury company"),
                            "TSLA", new TemplateSlot(8, "Tesla — EV + energy + autonomy"),
                            "COST", new TemplateSlot(7, "Costco — retail moat, membership growth"),
                            "VOO", new TemplateSlot(10, "S&P 500 — diversified growth anchor")
                    )),

            new TemplateDef("monthly-paycheck",
                    "Monthly Paycheck",
                    "Designed for investors who want predictable monthly income. Every holding pays monthly dividends for a steady cash flow.",
                    "Low-Medium", "Monthly Income", "$50k", 50_000,
                    Map.of(
                            "O", new TemplateSlot(20, "Realty Income — the monthly dividend company"),
                            "JEPI", new TemplateSlot(15, "JPMorgan premium income — 7.2% yield"),
                            "JEPQ", new TemplateSlot(12, "Nasdaq premium income — 8.5% yield"),
                            "MAIN", new TemplateSlot(10, "Main Street Capital — BDC, 6.9% yield"),
                            "STAG", new TemplateSlot(8, "STAG Industrial — industrial REIT, 4.5%"),
                            "SPHD", new TemplateSlot(8, "S&P 500 high dividend — low vol, 4.2%"),
                            "QQQI", new TemplateSlot(8, "Nasdaq 100 high income — 9.8% yield"),
                            "AGNC", new TemplateSlot(7, "AGNC Investment — mREIT, 14% yield"),
                            "PDI", new TemplateSlot(7, "PIMCO dynamic income — 13% yield"),
                            "SCHD", new TemplateSlot(5, "Dividend growth — long-term stability")
                    )),

            new TemplateDef("btc-strategy",
                    "Bitcoin Strategy",
                    "Crypto-adjacent equities: BTC treasury, miners, and spot ETFs. For investors who want Bitcoin exposure through regulated stocks.",
                    "Very High", "Crypto", "$25k", 25_000,
                    Map.of(
                            "MSTR", new TemplateSlot(30, "MicroStrategy — largest corporate BTC holder"),
                            "IBIT", new TemplateSlot(20, "iShares Bitcoin Trust — spot BTC ETF"),
                            "COIN", new TemplateSlot(15, "Coinbase — leading crypto exchange"),
                            "FBTC", new TemplateSlot(10, "Fidelity Bitcoin ETF — low-fee spot BTC"),
                            "MARA", new TemplateSlot(10, "MARA Holdings — largest public BTC miner"),
                            "RIOT", new TemplateSlot(8, "Riot Platforms — BTC mining infrastructure"),
                            "CLSK", new TemplateSlot(7, "CleanSpark — efficient BTC miner")
                    ))
    );
}
