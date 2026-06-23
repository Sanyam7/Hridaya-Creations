package com.hridayacreations.dto.response;

import com.hridayacreations.entity.enums.OrderStatus;
import com.hridayacreations.entity.enums.PaymentMethod;
import com.hridayacreations.entity.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Full representation of an order including snapshotted shipping address and price breakdown.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OrderResponse", description = "Order details")
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;

    private List<OrderItemResponse> items;
    private Integer totalItems;

    private BigDecimal subtotal;
    private BigDecimal gstAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal totalAmount;

    private String notes;
    private String cancelReason;

    private ShippingAddressResponse shippingAddress;

    private Instant placedAt;
    private Instant deliveredAt;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Snapshot of the shipping address as captured at order placement.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ShippingAddressResponse", description = "Snapshot of the delivery address")
    public static class ShippingAddressResponse {
        private String fullName;
        private String mobileNumber;
        private String houseNumber;
        private String street;
        private String city;
        private String state;
        private String country;
        private String pincode;
    }
}
