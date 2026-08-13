package com.hridayacreations.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A colour a product is available in. Always fully populated, so clients never have to infer a
 * display name or a swatch colour.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ProductColorResponse", description = "A colour a product is available in")
public class ProductColorResponse {

    @Schema(example = "red")
    private String id;

    @Schema(example = "Red")
    private String name;

    @Schema(example = "#E53935")
    private String hexCode;
}
