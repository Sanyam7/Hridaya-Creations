package com.hridayacreations.dto.request;

import com.hridayacreations.entity.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Admin payload to create a product. Images are managed separately via the image upload endpoints.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreateProductRequest", description = "Create product request")
public class CreateProductRequest {

    @Schema(example = "Personalized Magic Mug", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Schema(example = "A heat-sensitive magic mug that reveals your photo when filled with a hot beverage.")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Schema(example = "Heat-reveal personalized photo mug")
    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Category id is required")
    private Long categoryId;

    @Schema(example = "349.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Selling price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Selling price format is invalid")
    private BigDecimal sellingPrice;

    @Schema(example = "499.00")
    @DecimalMin(value = "0.0", inclusive = false, message = "Original price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Original price format is invalid")
    private BigDecimal originalPrice;

    @Schema(example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Schema(description = "Optional; auto-generated when blank", example = "HC-MUG-0001")
    @Size(max = 80, message = "SKU must not exceed 80 characters")
    private String sku;

    @Schema(description = "Defaults to ACTIVE when omitted", example = "ACTIVE")
    private ProductStatus productStatus;

    @Schema(example = "true")
    private Boolean featured;

    @Schema(example = "true")
    private Boolean customizable;

    @Schema(example = "[\"mug\", \"magic\", \"photo\", \"birthday\"]")
    private Set<String> tags;
}
