package com.hridayacreations.dto.mapper;

import com.hridayacreations.dto.response.ProductColorResponse;
import com.hridayacreations.dto.response.ProductCustomizationOptionResponse;
import com.hridayacreations.dto.response.ProductImageResponse;
import com.hridayacreations.dto.response.ProductResponse;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.ProductColor;
import com.hridayacreations.entity.ProductCustomizationOption;
import com.hridayacreations.entity.ProductImage;
import com.hridayacreations.service.support.CustomizationFieldSpec;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.List;

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
    @Mapping(target = "customizationOptions", ignore = true)   // built positionally in enrich()
    ProductResponse toResponse(Product product);

    ProductImageResponse toImageResponse(ProductImage image);

    /** {@code colorId} is the entity-side name for what the API exposes as {@code id}. */
    @Mapping(target = "id", source = "colorId")
    ProductColorResponse toColorResponse(ProductColor color);

    /**
     * Describes one configured field — built-in or admin-authored alike — so the storefront
     * receives a self-describing spec and never has to know the field list itself.
     *
     * <p>A built-in option whose catalog entry no longer exists resolves to nothing and is skipped
     * rather than returned half-built, so the storefront never renders a field it has no rules for.
     */
    default ProductCustomizationOptionResponse toCustomizationResponse(ProductCustomizationOption option) {
        // MapStruct maps the list element-wise, so the position is not available here; display
        // order is restored from the persisted list order in enrich() below.
        return CustomizationFieldSpec.resolve(option, 0)
                .map(spec -> ProductCustomizationOptionResponse.builder()
                        .key(spec.key())
                        .label(spec.label())
                        .required(spec.required())
                        .custom(spec.custom())
                        .fieldType(spec.fieldType())
                        .placeholder(spec.placeholder())
                        .maxLength(spec.maxLength())
                        .choices(spec.choices())
                        .minValue(spec.minValue())
                        .maxValue(spec.maxValue())
                        .build())
                .orElse(null);
    }

    /**
     * Derives stock availability and the primary image URL after the base mapping completes, and
     * restores the customization field list: the persisted order <em>is</em> the display order, and
     * any field that failed to resolve is dropped here rather than surfacing as a null element.
     */
    @AfterMapping
    default void enrich(Product product, @MappingTarget ProductResponse.ProductResponseBuilder builder) {
        List<ProductCustomizationOptionResponse> fields = new ArrayList<>();
        for (ProductCustomizationOption option : product.getCustomizationOptions()) {
            ProductCustomizationOptionResponse field = toCustomizationResponse(option);
            if (field != null) {
                field.setDisplayOrder(fields.size());
                fields.add(field);
            }
        }
        builder.customizationOptions(fields);

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
