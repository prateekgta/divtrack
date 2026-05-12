package io.divtrack.common;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void constructWithNullAmountThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Money(null, Currency.getInstance("USD")));
    }

    @Test
    void constructScalesAmountBeyond4() {
        Money m = Money.usd(1.12345);
        assertEquals(4, m.amount().scale());
    }

    @Test
    void constructDoesNotScaleAmountAtOrBelow4() {
        Money m = Money.usd(1.12);
        assertEquals(2, m.amount().scale());
    }

    @Test
    void usdDoubleFactory() {
        Money m = Money.usd(10.5);
        assertEquals(new BigDecimal("10.5"), m.amount());
        assertEquals(Currency.getInstance("USD"), m.currency());
    }

    @Test
    void usdBigDecimalFactory() {
        Money m = Money.usd(new BigDecimal("25.00"));
        assertEquals(new BigDecimal("25.00"), m.amount());
    }

    @Test
    void addSameCurrency() {
        Money a = Money.usd(10);
        Money b = Money.usd(5);
        Money result = a.add(b);
        assertEquals(new BigDecimal("15"), result.amount());
    }

    @Test
    void addDifferentCurrencyThrows() {
        Money a = Money.usd(10);
        Money b = new Money(BigDecimal.TEN, Currency.getInstance("EUR"));
        assertThrows(IllegalArgumentException.class, () -> a.add(b));
    }

    @Test
    void subtract() {
        Money a = Money.usd(10);
        Money b = Money.usd(3);
        Money result = a.subtract(b);
        assertEquals(new BigDecimal("7"), result.amount());
    }

    @Test
    void subtractDifferentCurrencyThrows() {
        Money a = Money.usd(10);
        Money b = new Money(BigDecimal.TEN, Currency.getInstance("EUR"));
        assertThrows(IllegalArgumentException.class, () -> a.subtract(b));
    }

    @Test
    void multiplyByBigDecimal() {
        Money m = Money.usd(10);
        Money result = m.multiply(new BigDecimal("2.5"));
        assertEquals(new BigDecimal("25.0"), result.amount());
    }

    @Test
    void multiplyByDouble() {
        Money m = Money.usd(10);
        Money result = m.multiply(1.5);
        assertEquals(new BigDecimal("15.0"), result.amount());
    }

    @Test
    void divide() {
        Money m = Money.usd(10);
        Money result = m.divide(new BigDecimal("3"));
        assertEquals(new BigDecimal("3.3333"), result.amount());
    }

    @Test
    void divideKeepsScaleOf4() {
        Money m = Money.usd(10);
        Money result = m.divide(new BigDecimal("3"));
        assertEquals(4, result.amount().scale());
    }

    @Test
    void toDouble() {
        Money m = Money.usd(15.75);
        assertEquals(15.75, m.toDouble(), 0.001);
    }

    @Test
    void toBigDecimal() {
        Money m = Money.usd(new BigDecimal("42.00"));
        assertEquals(new BigDecimal("42.00"), m.toBigDecimal());
    }

    @Test
    void toStringFormatsCorrectly() {
        Money m = Money.usd(12.5);
        assertEquals("$ 12.50", m.toString());
    }

    @Test
    void amountGetter() {
        Money m = Money.usd(7);
        assertEquals(new BigDecimal("7"), m.amount());
    }

    @Test
    void currencyGetter() {
        Money m = Money.usd(1);
        assertEquals(Currency.getInstance("USD"), m.currency());
    }

    @Test
    void usdConstant() {
        assertEquals(Currency.getInstance("USD"), Money.USD);
    }
}
