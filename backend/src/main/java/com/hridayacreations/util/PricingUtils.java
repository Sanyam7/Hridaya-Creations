package com.hridayacreations.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Catalog pricing helpers shared by mappers and services.
 */
public final class PricingUtils {

    private PricingUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Computes the discount percentage of a selling price relative to its original price.
     *
     * @return whole-number discount percentage, or {@code 0} when there is no valid discount
     */
    public static Integer discountPercentage(BigDecimal originalPrice, BigDecimal sellingPrice) {
        if (originalPrice == null || sellingPrice == null
                || originalPrice.compareTo(BigDecimal.ZERO) <= 0
                || sellingPrice.compareTo(originalPrice) >= 0) {
            return 0;
        }
        return originalPrice.subtract(sellingPrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(originalPrice, 0, RoundingMode.HALF_UP)
                .intValue();
    }
}
