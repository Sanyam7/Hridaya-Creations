package com.hridayacreations.dto.request;

import com.hridayacreations.entity.enums.CustomizationFieldType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One customization field the admin is configuring on a product. Listing a field enables it;
 * omitting it disables it. The list's order is the order customers see the fields in.
 *
 * <p>Two shapes share this payload:
 * <ul>
 *   <li><b>Built-in</b> ({@code custom} false/absent) — {@code key} names a supported option and
 *       the server-side catalog supplies its input kind and rules. Only {@code label},
 *       {@code placeholder}, {@code required} and a tighter {@code maxLength} are honoured.</li>
 *   <li><b>Custom</b> ({@code custom} true) — an admin-authored field, so {@code label} and
 *       {@code fieldType} are required and this payload is the whole definition. Send {@code key}
 *       back unchanged when editing an existing field; leave it out for a new one and the server
 *       generates a stable key.</li>
 * </ul>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ProductCustomizationOptionRequest", description = "A customization field to configure")
public class ProductCustomizationOptionRequest {

    @Schema(example = "customerName",
            description = "Required for a built-in option. For a custom field, send back the key of "
                    + "an existing field; omit it for a new one.")
    @Size(max = 60, message = "Customization option key must not exceed 60 characters")
    private String key;

    @Schema(example = "Enter Your Name",
            description = "Required for a custom field; falls back to the catalog label for a built-in one")
    @Size(max = 120, message = "Customization label must not exceed 120 characters")
    private String label;

    @Schema(example = "true", description = "Whether the customer must supply a value; defaults to false")
    private Boolean required;

    @Schema(example = "false",
            description = "True for an admin-authored field, false/absent for a built-in option")
    private Boolean custom;

    @Schema(example = "NUMBER",
            description = "Required for a custom field: TEXT, TEXTAREA, NUMBER, BOOLEAN or DATE. "
                    + "Ignored for a built-in option, whose type the catalog owns.")
    private CustomizationFieldType fieldType;

    @Schema(example = "Type your name", description = "Hint text shown inside free-text inputs")
    @Size(max = 120, message = "Placeholder must not exceed 120 characters")
    private String placeholder;

    @Schema(example = "30",
            description = "Character cap for a text field. For a built-in option this may only "
                    + "tighten the catalog's cap, never loosen it.")
    @Min(value = 1, message = "Maximum length must be at least 1")
    private Integer maxLength;

    @Schema(example = "1", description = "Inclusive lower bound for a NUMBER field")
    private BigDecimal minValue;

    @Schema(example = "100", description = "Inclusive upper bound for a NUMBER field")
    private BigDecimal maxValue;
}
