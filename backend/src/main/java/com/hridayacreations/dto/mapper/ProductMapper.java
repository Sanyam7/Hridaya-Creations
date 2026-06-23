package com.hridayacreations.dto.mapper;

import com.hridayacreations.dto.response.ProductImageResponse;
import com.hridayacreations.dto.response.ProductResponse;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.ProductImage;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Maps {@link Product} entities to {@link ProductResponse}. Entity creation/updates are performed
 * explicitly in the service layer (enum/boolean defaults), so this mapper is read-only.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.categoryName")
    @Mapping(target = "discountPercentage",
            expression = "java(com.hridayacreations.util.PricingUtils.discountPercentage("
                    + "product.getOriginalPrice(), product.getSellingPrice()))")
    @Mapping(target = "inStock", ignore = true)
    @Mapping(target = "primaryImageUrl", ignore = true)
    ProductResponse toResponse(Product product);

    ProductImageResponse toImageResponse(ProductImage image);

    /**
     * Derives stock availability and the primary image URL after the base mapping completes.
     */
    @AfterMapping
    default void enrich(Product product, @MappingTarget ProductResponse.ProductResponseBuilder builder) {
        builder.inStock(product.isInStock());
        String primaryUrl = product.getImages().stream()
                .filter(ProductImage::isPrimaryImage)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElseGet(() -> product.getImages().isEmpty()
                        ? null : product.getImages().get(0).getImageUrl());
        builder.primaryImageUrl(primaryUrl);
    }
}
