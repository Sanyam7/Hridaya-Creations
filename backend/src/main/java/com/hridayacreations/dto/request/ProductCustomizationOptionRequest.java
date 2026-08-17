package com.hridayacreations.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One customization option the admin is switching on for a product. Listing an option enables it;
 * omitting it disables it. The input kind and validation rules come from the server-side catalog,
 * so only the label and requiredness can be varied per product.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ProductCustomizationOptionRequest", description = "A customization option to enable")
public class ProductCustomizationOptionRequest {

    @Schema(example = "customerName", requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Must be one of the supported option keys")
    @NotBlank(message = "Customization option key is required")
    @Size(max = 60, message = "Customization option key must not exceed 60 characters")
    private String key;

    @Schema(example = "Enter Your Name", description = "Optional; falls back to the catalog label")
    @Size(max = 120, message = "Customization label must not exceed 120 characters")
    private String label;

    @Schema(example = "true", description = "Whether the customer must supply a value; defaults to false")
    private Boolean required;
}
