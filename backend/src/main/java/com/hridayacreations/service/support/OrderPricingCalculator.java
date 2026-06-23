package com.hridayacreations.service.support;

import com.hridayacreations.config.AppProperties;
import com.hridayacreations.util.MoneyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Computes the GST, delivery and grand-total breakdown for a given subtotal using the configured
 * {@code app.order.*} rates. Shared by the cart and order services to keep pricing consistent.
 */
@Component
@RequiredArgsConstructor
public class OrderPricingCalculator {

    private final AppProperties appProperties;

    public PriceBreakdown calculate(BigDecimal rawSubtotal) {
        AppProperties.Order config = appProperties.getOrder();
        BigDecimal subtotal = MoneyUtils.scale(rawSubtotal);
        BigDecimal gstAmount = MoneyUtils.percentageOf(subtotal, config.getGstPercentage());

        BigDecimal deliveryCharge = BigDecimal.ZERO;
        boolean hasItems = subtotal.compareTo(BigDecimal.ZERO) > 0;
        if (hasItems && subtotal.compareTo(config.getFreeDeliveryThreshold()) < 0) {
            deliveryCharge = config.getDeliveryCharge();
        }
        deliveryCharge = MoneyUtils.scale(deliveryCharge);

        BigDecimal total = MoneyUtils.scale(subtotal.add(gstAmount).add(deliveryCharge));
        return new PriceBreakdown(subtotal, config.getGstPercentage(), gstAmount, deliveryCharge, total);
    }

    /**
     * Immutable price breakdown for a cart or order.
     */
    public record PriceBreakdown(
            BigDecimal subtotal,
            BigDecimal gstPercentage,
            BigDecimal gstAmount,
            BigDecimal deliveryCharge,
            BigDecimal totalAmount) {
    }
}
