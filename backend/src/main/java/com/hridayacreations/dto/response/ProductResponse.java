package com.hridayacreations.dto.response;

import com.hridayacreations.entity.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Detailed representation of a product, including denormalized category info, computed discount and
 * stock availability, image list and aggregate rating.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ProductResponse", description = "Product details")
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String shortDescription;
    private Long categoryId;
    private String categoryName;
    private BigDecimal sellingPrice;
    private BigDecimal originalPrice;
    private Integer discountPercentage;
    private Integer stockQuantity;
    private boolean inStock;
    private String sku;
    private ProductStatus productStatus;
    private boolean featured;
    private boolean customizable;
    private Set<String> tags;
    private List<ProductImageResponse> images;
    private String primaryImageUrl;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Instant createdAt;
    private Instant updatedAt;
}
