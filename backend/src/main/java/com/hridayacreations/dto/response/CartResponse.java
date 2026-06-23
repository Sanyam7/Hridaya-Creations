package com.hridayacreations.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * The customer's cart with line items and a fully computed price breakdown
 * (subtotal, GST, delivery and final payable amount).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CartResponse", description = "Cart with computed price breakdown")
public class CartResponse {

    private Long id;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Integer totalQuantity;

    @Schema(description = "Sum of all line totals", example = "698.00")
    private BigDecimal subtotal;

    @Schema(description = "GST computed on the subtotal", example = "125.64")
    private BigDecimal gstAmount;

    @Schema(description = "GST percentage applied", example = "18")
    private BigDecimal gstPercentage;

    @Schema(description = "Delivery charge (0 above the free-delivery threshold)", example = "0.00")
    private BigDecimal deliveryCharge;

    @Schema(description = "Final payable amount", example = "823.64")
    private BigDecimal totalAmount;
}
