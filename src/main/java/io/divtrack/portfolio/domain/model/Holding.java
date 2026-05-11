package io.divtrack.portfolio.domain.model;

import io.divtrack.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "holdings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "stock_id"})
})
public class Holding extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Column(name = "stock_id", nullable = false, length = 26)
    private String stockId;

    @Column(nullable = false, precision = 14, scale = 6)
    private BigDecimal shares;

    @Column(name = "cost_basis", precision = 12, scale = 2)
    private BigDecimal costBasis;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType = AccountType.TAXABLE;

    @Column(length = 500)
    private String notes;

    public Holding() {}

    public Holding(String userId, String stockId, BigDecimal shares, BigDecimal costBasis, AccountType accountType) {
        this.userId = userId;
        this.stockId = stockId;
        this.shares = shares;
        this.costBasis = costBasis;
        this.accountType = accountType;
    }

    public BigDecimal getValue(BigDecimal price) {
        return shares.multiply(price);
    }

    public BigDecimal projectedMonthlyIncome(BigDecimal price, BigDecimal yieldPct, String frequency) {
        BigDecimal annualIncome = shares.multiply(price).multiply(yieldPct.divide(BigDecimal.valueOf(100), 6, java.math.RoundingMode.HALF_UP));
        return switch (frequency.toUpperCase()) {
            case "MONTHLY" -> annualIncome.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
            case "QUARTERLY" -> annualIncome.divide(BigDecimal.valueOf(4), 2, java.math.RoundingMode.HALF_UP);
            case "SEMI_ANNUAL" -> annualIncome.divide(BigDecimal.valueOf(6), 2, java.math.RoundingMode.HALF_UP);
            default -> annualIncome.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
        };
    }

    public String getUserId() { return userId; }
    public String getStockId() { return stockId; }
    public BigDecimal getShares() { return shares; }
    public BigDecimal getCostBasis() { return costBasis; }
    public AccountType getAccountType() { return accountType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
