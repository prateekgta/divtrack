package io.divtrack.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public static final Currency USD = Currency.getInstance("USD");

    public Money {
        if (amount == null) throw new IllegalArgumentException("amount must not be null");
        if (amount.scale() > 4) {
            amount = amount.setScale(4, RoundingMode.HALF_UP);
        }
    }

    public static Money usd(double amount) {
        return new Money(BigDecimal.valueOf(amount), USD);
    }

    public static Money usd(BigDecimal amount) {
        return new Money(amount, USD);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }

    public Money multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    public Money divide(BigDecimal divisor) {
        return new Money(this.amount.divide(divisor, 4, RoundingMode.HALF_UP), this.currency);
    }

    public double toDouble() {
        return amount.doubleValue();
    }

    public BigDecimal toBigDecimal() {
        return amount;
    }

    @Override
    public String toString() {
        return String.format("%s %.2f", currency.getSymbol(), amount);
    }
}
