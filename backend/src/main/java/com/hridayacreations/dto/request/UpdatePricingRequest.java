package com.hridayacreations.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Admin payload to update only a product's pricing.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UpdatePricingRequest", description = "Update product pricing request")
public class UpdatePricingRequest {

    @Schema(example = "299.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Selling price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Selling price format is invalid")
    private BigDecimal sellingPrice;

    @Schema(example = "499.00")
    @DecimalMin(value = "0.0", inclusive = false, message = "Original price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Original price format is invalid")
    private BigDecimal originalPrice;
}
