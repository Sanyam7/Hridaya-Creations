package com.hridayacreations.dto.response;

import com.hridayacreations.entity.enums.CustomizationFieldType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One customization value on a cart or order line, with the label and input kind it was answered
 * under.
 *
 * <p>Self-describing by design: an order line is readable straight from this, with no lookup
 * against the product's current configuration — which is what keeps a past order intact after the
 * admin renames, retypes or deletes the field it answered.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CustomizationValueResponse", description = "A submitted customization value")
public class CustomizationValueResponse {

    @Schema(example = "cf_luckyNumber")
    private String key;

    @Schema(example = "Enter Your Lucky Number", description = "The label as shown when answered")
    private String label;

    @Schema(example = "NUMBER")
    private CustomizationFieldType fieldType;

    /**
     * Typed to match {@link #fieldType}: a JSON boolean for BOOLEAN, a JSON number for NUMBER, a
     * string otherwise — never the string {@code "false"} for a boolean, which every language
     * treats as truthy.
     */
    @Schema(example = "7", description = "Typed per fieldType: boolean, number or string")
    private Object value;
}
