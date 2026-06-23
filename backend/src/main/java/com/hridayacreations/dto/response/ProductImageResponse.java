package com.hridayacreations.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representation of a single product image.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ProductImageResponse", description = "Product image details")
public class ProductImageResponse {

    private Long id;
    private String imageUrl;
    private String publicId;
    private boolean primaryImage;
    private Integer displayOrder;
}
