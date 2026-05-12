package io.divtrack.common;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PercentageTest {

    @Test
    void constructWithZero() {
        Percentage p = new Percentage(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, p.value());
    }

    @Test
    void constructWithFifty() {
        Percentage p = Percentage.of(50);
        assertEquals(BigDecimal.valueOf(50), p.value());
    }

    @Test
    void constructWithOneHundred() {
        Percentage p = Percentage.of(100);
        assertEquals(BigDecimal.valueOf(100), p.value());
    }

    @Test
    void constructWithNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Percentage(null));
    }

    @Test
    void constructWithNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> Percentage.of(-1));
    }

    @Test
    void constructWithAboveOneHundredThrows() {
        assertThrows(IllegalArgumentException.class, () -> Percentage.of(101));
    }

    @Test
    void ofBigDecimalFactory() {
        Percentage p = Percentage.of(new BigDecimal("75"));
        assertEquals(BigDecimal.valueOf(75), p.value());
    }

    @Test
    void asDecimalForFifty() {
        Percentage p = Percentage.of(50);
        assertEquals(new BigDecimal("0.500000"), p.asDecimal());
    }

    @Test
    void asDecimalForOneHundred() {
        Percentage p = Percentage.of(100);
        assertEquals(BigDecimal.ONE.setScale(6), p.asDecimal());
    }

    @Test
    void asDecimalForZero() {
        Percentage p = Percentage.of(0);
        assertEquals(BigDecimal.ZERO.setScale(6), p.asDecimal());
    }

    @Test
    void toDouble() {
        Percentage p = Percentage.of(33.3);
        assertEquals(33.3, p.toDouble(), 0.001);
    }

    @Test
    void isGreaterThanReturnsTrue() {
        Percentage a = Percentage.of(50);
        Percentage b = Percentage.of(25);
        assertTrue(a.isGreaterThan(b));
    }

    @Test
    void isGreaterThanReturnsFalse() {
        Percentage a = Percentage.of(25);
        Percentage b = Percentage.of(50);
        assertFalse(a.isGreaterThan(b));
    }

    @Test
    void isGreaterThanReturnsFalseForEqual() {
        Percentage a = Percentage.of(50);
        Percentage b = Percentage.of(50);
        assertFalse(a.isGreaterThan(b));
    }

    @Test
    void toStringFormatsCorrectly() {
        Percentage p = Percentage.of(50);
        assertEquals("50.00%", p.toString());
    }

    @Test
    void toStringForZero() {
        Percentage p = Percentage.of(0);
        assertEquals("0.00%", p.toString());
    }

    @Test
    void toStringForOneHundred() {
        Percentage p = Percentage.of(100);
        assertEquals("100.00%", p.toString());
    }
}
