package com.hridayacreations.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A wishlist entry with a lightweight product summary.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "WishlistResponse", description = "Wishlist entry with product summary")
public class WishlistResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal sellingPrice;
    private BigDecimal originalPrice;
    private boolean inStock;
    private Instant addedAt;
}
