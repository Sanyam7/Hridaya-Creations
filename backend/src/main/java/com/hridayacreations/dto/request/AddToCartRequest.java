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
     * The customer's personalisation, keyed by customization field. Values are typed to match the
     * field: a JSON string for text, a JSON number for NUMBER, a JSON boolean for BOOLEAN.
     *
     * <p>Validated server-side against the product's stored configuration — fields the admin did
     * not configure are rejected, and a readymade product rejects any customization at all. Note
     * that {@code false} is a submitted answer, not an omission: only leaving a key out (or sending
     * null) counts as unanswered.
     */
    @Schema(description = "Customization values keyed by field; omit for readymade products",
            example = "{\"customerName\": \"John\", \"cf_luckyNumber\": 7, \"cf_giftWrap\": false}")
    private Map<String, Object> customization;
}
