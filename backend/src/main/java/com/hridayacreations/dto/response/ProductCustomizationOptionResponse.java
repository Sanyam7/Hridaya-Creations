package com.hridayacreations.dto.response;

import com.hridayacreations.entity.enums.CustomizationFieldType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * A customization option enabled on a product, carrying everything the storefront needs to render
 * the control and validate the value client-side — so no field list is ever hardcoded in the UI.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ProductCustomizationOptionResponse", description = "An enabled customization option")
public class ProductCustomizationOptionResponse {

    @Schema(example = "customerName")
    private String key;

    @Schema(example = "TEXT")
    private CustomizationFieldType fieldType;

    @Schema(example = "Name / Text to Print")
    private String label;

    @Schema(example = "true")
    private boolean required;

    @Schema(example = "60", description = "Character limit for TEXT/TEXTAREA; null otherwise")
    private Integer maxLength;

    @Schema(description = "Allowed values for SELECT fields; empty otherwise")
    private List<String> choices;

    @Schema(example = "1")
    private int displayOrder;
}
