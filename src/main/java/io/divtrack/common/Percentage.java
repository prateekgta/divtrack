package io.divtrack.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Percentage(BigDecimal value) {

    public Percentage {
        if (value == null) throw new IllegalArgumentException("value must not be null");
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.valueOf(100000)) > 0) {
            throw new IllegalArgumentException("percentage must be between 0 and 100000");
        }
    }

    public static Percentage of(double value) {
        return new Percentage(BigDecimal.valueOf(value));
    }

    public static Percentage of(BigDecimal value) {
        return new Percentage(value);
    }

    public BigDecimal asDecimal() {
        return value.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    }

    public double toDouble() {
        return value.doubleValue();
    }

    public boolean isGreaterThan(Percentage other) {
        return this.value.compareTo(other.value) > 0;
    }

    @Override
    public String toString() {
        return String.format("%.2f%%", value);
    }
}
