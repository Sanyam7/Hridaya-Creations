package com.hridayacreations.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One colour in a product's colour selection. For a predefined colour the {@code id} alone is
 * enough — the server fills in the canonical name and hex code. A custom colour must additionally
 * carry a name and a valid hex code.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ProductColorRequest", description = "A colour a product is available in")
public class ProductColorRequest {

    @Schema(example = "red", description = "Stable colour key; slugified server-side")
    @Size(max = 40, message = "Colour id must not exceed 40 characters")
    private String id;

    @Schema(example = "Red", description = "Required for custom colours; derived from the palette otherwise")
    @Size(max = 60, message = "Colour name must not exceed 60 characters")
    private String name;

    @Schema(example = "#E53935", description = "Required for custom colours; derived from the palette otherwise")
    @Pattern(regexp = "^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$",
            message = "Hex code must look like #RGB or #RRGGBB")
    private String hexCode;
}
