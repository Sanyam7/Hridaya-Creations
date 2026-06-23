package com.hridayacreations.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Adds a product to the authenticated user's wishlist.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AddToWishlistRequest", description = "Add-to-wishlist request")
public class AddToWishlistRequest {

    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Product id is required")
    private Long productId;
}
