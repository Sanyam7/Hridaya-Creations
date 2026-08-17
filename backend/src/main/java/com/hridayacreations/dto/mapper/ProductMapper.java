package com.hridayacreations.dto.mapper;

import com.hridayacreations.dto.response.ProductColorResponse;
import com.hridayacreations.dto.response.ProductCustomizationOptionResponse;
import com.hridayacreations.dto.response.ProductImageResponse;
import com.hridayacreations.dto.response.ProductResponse;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.ProductColor;
import com.hridayacreations.entity.ProductCustomizationOption;
import com.hridayacreations.entity.ProductImage;
import com.hridayacreations.service.support.CustomizationCatalog;
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

    /** {@code colorId} is the entity-side name for what the API exposes as {@code id}. */
    @Mapping(target = "id", source = "colorId")
    ProductColorResponse toColorResponse(ProductColor color);

    /**
     * Combines the per-product configuration (label, requiredness) with the catalog definition
     * (input kind, limits, choices) so the storefront receives one self-describing field spec and
     * never has to know the option list itself.
     */
    default ProductCustomizationOptionResponse toCustomizationResponse(ProductCustomizationOption option) {
        if (option == null) {
            return null;
        }
        return CustomizationCatalog.find(option.getOptionKey())
                .map(definition -> ProductCustomizationOptionResponse.builder()
                        .key(option.getOptionKey())
                        .label(option.getLabel())
                        .required(option.isRequired())
                        .fieldType(definition.fieldType())
                        .maxLength(definition.maxLength())
                        .choices(definition.choices())
                        .displayOrder(definition.displayOrder())
                        .build())
                // A key that is no longer in the catalog (an option retired after products used it)
                // is skipped rather than returned half-built, so the storefront never renders a
                // field it has no rules for.
                .orElse(null);
    }

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
