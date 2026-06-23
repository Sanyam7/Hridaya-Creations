package com.hridayacreations.dto.mapper;

import com.hridayacreations.dto.response.WishlistResponse;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.ProductImage;
import com.hridayacreations.entity.Wishlist;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Maps {@link Wishlist} entities to {@link WishlistResponse}, flattening a lightweight product
 * summary and the primary image URL.
 */
@Mapper(componentModel = "spring")
public interface WishlistMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "sellingPrice", source = "product.sellingPrice")
    @Mapping(target = "originalPrice", source = "product.originalPrice")
    @Mapping(target = "addedAt", source = "createdAt")
    @Mapping(target = "inStock", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    WishlistResponse toResponse(Wishlist wishlist);

    @AfterMapping
    default void enrich(Wishlist wishlist, @MappingTarget WishlistResponse.WishlistResponseBuilder builder) {
        Product product = wishlist.getProduct();
        builder.inStock(product.isInStock());
        String primaryUrl = product.getImages().stream()
                .filter(ProductImage::isPrimaryImage)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElseGet(() -> product.getImages().isEmpty()
                        ? null : product.getImages().get(0).getImageUrl());
        builder.imageUrl(primaryUrl);
    }
}
