package com.hridayacreations.dto.response;

import com.hridayacreations.entity.enums.CustomizationFieldType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * A customization field configured on a product, carrying everything the storefront needs to render
 * the control and validate the value client-side — so no field list is ever hardcoded in the UI.
 *
 * <p>Built-in options and admin-authored custom fields are described identically here. A client
 * renders from {@link #fieldType} and never needs to know which kind it is looking at; {@link
 * #custom} exists for the admin form, which does need to tell them apart to know what is editable.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ProductCustomizationOptionResponse", description = "A configured customization field")
public class ProductCustomizationOptionResponse {

    @Schema(example = "customerName", description = "Stable key; custom fields are prefixed 'cf_'")
    private String key;

    @Schema(example = "TEXT")
    private CustomizationFieldType fieldType;

    @Schema(example = "Name / Text to Print")
    private String label;

    @Schema(example = "true")
    private boolean required;

    @Schema(example = "false", description = "True for an admin-authored field")
    private boolean custom;

    @Schema(example = "Type your name", description = "Hint text for free-text inputs; null if unset")
    private String placeholder;

    @Schema(example = "60", description = "Character limit for TEXT/TEXTAREA; null otherwise")
    private Integer maxLength;

    @Schema(description = "Allowed values for SELECT fields; empty otherwise")
    private List<String> choices;

    @Schema(example = "1", description = "Inclusive lower bound for NUMBER fields; null if unbounded")
    private BigDecimal minValue;

    @Schema(example = "100", description = "Inclusive upper bound for NUMBER fields; null if unbounded")
    private BigDecimal maxValue;

    @Schema(example = "1")
    private int displayOrder;
}
