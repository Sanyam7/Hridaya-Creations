package com.hridayacreations.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Adds a product to the authenticated user's cart. An identical line (same product and same
 * customization) has its quantity increased; a differently-personalised one becomes its own line.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AddToCartRequest", description = "Add-to-cart request")
public class AddToCartRequest {

    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Product id is required")
    private Long productId;

    @Schema(example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    private Integer quantity;

    /**
     * The customer's personalisation, keyed by customization option. Validated server-side against
     * the product's stored configuration — options the admin did not enable are rejected, and a
     * readymade product rejects any customization at all.
     */
    @Schema(description = "Customization values keyed by option; omit for readymade products",
            example = "{\"customerName\": \"John\"}")
    private Map<String, String> customization;
}
