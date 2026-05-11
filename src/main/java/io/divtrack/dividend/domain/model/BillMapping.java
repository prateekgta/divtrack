package io.divtrack.dividend.domain.model;

import io.divtrack.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bill_mappings")
public class BillMapping extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Column(name = "stock_id", nullable = false, length = 26)
    private String stockId;

    @Column(name = "bill_name", nullable = false, length = 100)
    private String billName;

    @Column(name = "bill_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal billAmount;

    public BillMapping() {}

    public BillMapping(String userId, String stockId, String billName, BigDecimal billAmount) {
        this.userId = userId;
        this.stockId = stockId;
        this.billName = billName;
        this.billAmount = billAmount;
    }

    public BigDecimal sharesRequiredForCoverage(BigDecimal stockPrice, BigDecimal yieldPct, String frequency) {
        BigDecimal annualIncomePerShare = stockPrice.multiply(yieldPct.divide(BigDecimal.valueOf(100), 6, java.math.RoundingMode.HALF_UP));
        BigDecimal annualBill = billAmount.multiply(BigDecimal.valueOf(12));
        if (annualIncomePerShare.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return annualBill.divide(annualIncomePerShare, 0, java.math.RoundingMode.UP);
    }

    public String getUserId() { return userId; }
    public String getStockId() { return stockId; }
    public String getBillName() { return billName; }
    public BigDecimal getBillAmount() { return billAmount; }
}
