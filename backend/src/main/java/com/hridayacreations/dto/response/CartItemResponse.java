package com.hridayacreations.dto.response;

import com.hridayacreations.entity.enums.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * A single line item in the cart, with live stock availability for the referenced product.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CartItemResponse", description = "Cart line item")
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private String imageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
    private boolean inStock;
    private Integer availableStock;

    /** Lets the cart UI branch without re-fetching the product. */
    private ProductType productType;

    /** The customer's personalisation for this line; empty for readymade products. */
    private Map<String, String> customization;
}
