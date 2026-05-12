package io.divtrack.dividend.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BillMappingTest {

    @Test
    void constructorSetsFields() {
        BillMapping b = new BillMapping("user1", "stock1", "Electric Bill", BigDecimal.valueOf(150));
        assertEquals("user1", b.getUserId());
        assertEquals("stock1", b.getStockId());
        assertEquals("Electric Bill", b.getBillName());
        assertEquals(BigDecimal.valueOf(150), b.getBillAmount());
    }

    @Test
    void sharesRequiredForCoverageCalculatesCorrectly() {
        BillMapping b = new BillMapping("user1", "stock1", "Rent", BigDecimal.valueOf(2000));
        BigDecimal shares = b.sharesRequiredForCoverage(BigDecimal.valueOf(100), BigDecimal.valueOf(6), "MONTHLY");
        assertEquals(BigDecimal.valueOf(4000), shares);
    }

    @Test
    void sharesRequiredForCoverageRoundsUp() {
        BillMapping b = new BillMapping("user1", "stock1", "Internet", BigDecimal.valueOf(80));
        BigDecimal shares = b.sharesRequiredForCoverage(BigDecimal.valueOf(100), BigDecimal.valueOf(3), "MONTHLY");
        assertEquals(BigDecimal.valueOf(320), shares);
    }

    @Test
    void sharesRequiredForCoverageWithZeroYieldReturnsZero() {
        BillMapping b = new BillMapping("user1", "stock1", "Netflix", BigDecimal.valueOf(15.99));
        BigDecimal shares = b.sharesRequiredForCoverage(BigDecimal.valueOf(100), BigDecimal.ZERO, "MONTHLY");
        assertEquals(BigDecimal.ZERO, shares);
    }

    @Test
    void sharesRequiredForCoverageWithSmallYield() {
        BillMapping b = new BillMapping("user1", "stock1", "Subscription", BigDecimal.valueOf(10));
        BigDecimal shares = b.sharesRequiredForCoverage(BigDecimal.valueOf(200), BigDecimal.valueOf(0.5), "MONTHLY");
        assertEquals(BigDecimal.valueOf(120), shares);
    }

    @Test
    void sharesRequiredForCoverageWithZeroStockPrice() {
        BillMapping b = new BillMapping("user1", "stock1", "Bill", BigDecimal.valueOf(100));
        BigDecimal shares = b.sharesRequiredForCoverage(BigDecimal.ZERO, BigDecimal.valueOf(5), "MONTHLY");
        assertEquals(BigDecimal.ZERO, shares);
    }

    @Test
    void sharesRequiredForCoverageWithHighYield() {
        BillMapping b = new BillMapping("user1", "stock1", "Car Payment", BigDecimal.valueOf(500));
        BigDecimal shares = b.sharesRequiredForCoverage(BigDecimal.valueOf(50), BigDecimal.valueOf(12), "MONTHLY");
        assertEquals(BigDecimal.valueOf(1000), shares);
    }
}
