package io.divtrack.portfolio.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HoldingTest {

    @Test
    void constructorSetsFields() {
        Holding h = new Holding("user1", "stock1", BigDecimal.valueOf(100), BigDecimal.valueOf(5000), AccountType.TAXABLE);
        assertEquals("user1", h.getUserId());
        assertEquals("stock1", h.getStockId());
        assertEquals(BigDecimal.valueOf(100), h.getShares());
        assertEquals(BigDecimal.valueOf(5000), h.getCostBasis());
        assertEquals(AccountType.TAXABLE, h.getAccountType());
    }

    @Test
    void getValueMultipliesSharesByPrice() {
        Holding h = new Holding("user1", "stock1", BigDecimal.valueOf(50), BigDecimal.valueOf(2500), AccountType.ROTH_IRA);
        BigDecimal value = h.getValue(BigDecimal.valueOf(30));
        assertEquals(BigDecimal.valueOf(1500), value);
    }

    @Test
    void getValueWithZeroShares() {
        Holding h = new Holding("user1", "stock1", BigDecimal.ZERO, BigDecimal.ZERO, AccountType.TAXABLE);
        BigDecimal value = h.getValue(BigDecimal.valueOf(100));
        assertEquals(BigDecimal.ZERO, value);
    }

    @Test
    void getValueWithZeroPrice() {
        Holding h = new Holding("user1", "stock1", BigDecimal.valueOf(100), BigDecimal.valueOf(5000), AccountType.TAXABLE);
        BigDecimal value = h.getValue(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, value);
    }

    @Test
    void projectedMonthlyIncomeForMonthlyFrequency() {
        Holding h = new Holding("user1", "stock1", BigDecimal.valueOf(100), BigDecimal.valueOf(5000), AccountType.TAXABLE);
        BigDecimal income = h.projectedMonthlyIncome(BigDecimal.valueOf(100), BigDecimal.valueOf(6), "MONTHLY");
        assertEquals(new BigDecimal("50.00"), income);
    }

    @Test
    void projectedMonthlyIncomeForQuarterlyFrequency() {
        Holding h = new Holding("user1", "stock1", BigDecimal.valueOf(100), BigDecimal.valueOf(5000), AccountType.TAXABLE);
        BigDecimal income = h.projectedMonthlyIncome(BigDecimal.valueOf(100), BigDecimal.valueOf(6), "QUARTERLY");
        assertEquals(new BigDecimal("150.00"), income);
    }

    @Test
    void projectedMonthlyIncomeForSemiAnnualFrequency() {
        Holding h = new Holding("user1", "stock1", BigDecimal.valueOf(100), BigDecimal.valueOf(5000), AccountType.TAXABLE);
        BigDecimal income = h.projectedMonthlyIncome(BigDecimal.valueOf(100), BigDecimal.valueOf(6), "SEMI_ANNUAL");
        assertEquals(new BigDecimal("100.00"), income);
    }

    @Test
    void projectedMonthlyIncomeWithZeroYield() {
        Holding h = new Holding("user1", "stock1", BigDecimal.valueOf(100), BigDecimal.valueOf(5000), AccountType.TAXABLE);
        BigDecimal income = h.projectedMonthlyIncome(BigDecimal.valueOf(100), BigDecimal.ZERO, "MONTHLY");
        assertEquals(BigDecimal.ZERO.setScale(2), income);
    }

    @Test
    void projectedMonthlyIncomeDefaultFrequency() {
        Holding h = new Holding("user1", "stock1", BigDecimal.valueOf(100), BigDecimal.valueOf(5000), AccountType.TAXABLE);
        BigDecimal income = h.projectedMonthlyIncome(BigDecimal.valueOf(100), BigDecimal.valueOf(6), "ANNUAL");
        assertEquals(new BigDecimal("50.00"), income);
    }

    @Test
    void projectedMonthlyIncomeWithFractionalShares() {
        Holding h = new Holding("user1", "stock1", BigDecimal.valueOf(10.5), BigDecimal.valueOf(500), AccountType.TAXABLE);
        BigDecimal income = h.projectedMonthlyIncome(BigDecimal.valueOf(100), BigDecimal.valueOf(4), "MONTHLY");
        assertEquals(new BigDecimal("3.50"), income);
    }

    @Test
    void setAndGetNotes() {
        Holding h = new Holding("user1", "stock1", BigDecimal.valueOf(100), BigDecimal.valueOf(5000), AccountType.TAXABLE);
        h.setNotes("test note");
        assertEquals("test note", h.getNotes());
    }
}
