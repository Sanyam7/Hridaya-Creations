package com.hridayacreations.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Helpers for consistent monetary arithmetic — all amounts are normalised to two decimal places
 * using banker-friendly {@link RoundingMode#HALF_UP}.
 */
public final class MoneyUtils {

    public static final int SCALE = 2;

    private MoneyUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * @return {@code value} scaled to 2 decimals, or {@code 0.00} when {@code value} is null
     */
    public static BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Computes a percentage of a base amount, scaled to 2 decimals.
     *
     * @param base       the base amount
     * @param percentage the percentage to apply (e.g. {@code 18})
     */
    public static BigDecimal percentageOf(BigDecimal base, BigDecimal percentage) {
        if (base == null || percentage == null) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return base.multiply(percentage)
                .divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP);
    }
}
