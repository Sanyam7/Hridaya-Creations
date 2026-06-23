package com.hridayacreations.service;

import com.hridayacreations.config.AppProperties;
import com.hridayacreations.service.support.OrderPricingCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies GST, delivery and grand-total computation, including the free-delivery threshold.
 */
class OrderPricingCalculatorTest {

    private OrderPricingCalculator calculator;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        properties.getOrder().setGstPercentage(new BigDecimal("18"));
        properties.getOrder().setDeliveryCharge(new BigDecimal("50"));
        properties.getOrder().setFreeDeliveryThreshold(new BigDecimal("500"));
        calculator = new OrderPricingCalculator(properties);
    }

    @Test
    @DisplayName("applies GST and delivery charge below the free-delivery threshold")
    void belowThreshold() {
        OrderPricingCalculator.PriceBreakdown breakdown = calculator.calculate(new BigDecimal("200"));

        assertThat(breakdown.subtotal()).isEqualByComparingTo("200.00");
        assertThat(breakdown.gstAmount()).isEqualByComparingTo("36.00");
        assertThat(breakdown.deliveryCharge()).isEqualByComparingTo("50.00");
        assertThat(breakdown.totalAmount()).isEqualByComparingTo("286.00");
    }

    @Test
    @DisplayName("waives delivery charge at or above the free-delivery threshold")
    void freeDelivery() {
        OrderPricingCalculator.PriceBreakdown breakdown = calculator.calculate(new BigDecimal("600"));

        assertThat(breakdown.gstAmount()).isEqualByComparingTo("108.00");
        assertThat(breakdown.deliveryCharge()).isEqualByComparingTo("0.00");
        assertThat(breakdown.totalAmount()).isEqualByComparingTo("708.00");
    }

    @Test
    @DisplayName("charges nothing for an empty subtotal")
    void emptyCart() {
        OrderPricingCalculator.PriceBreakdown breakdown = calculator.calculate(BigDecimal.ZERO);

        assertThat(breakdown.gstAmount()).isEqualByComparingTo("0.00");
        assertThat(breakdown.deliveryCharge()).isEqualByComparingTo("0.00");
        assertThat(breakdown.totalAmount()).isEqualByComparingTo("0.00");
    }
}
