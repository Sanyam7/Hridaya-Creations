package com.hridayacreations.dto.mapper;

import com.hridayacreations.dto.response.ReviewResponse;
import com.hridayacreations.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link Review} entities to {@link ReviewResponse} DTOs.
 */
@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "reviewerName", source = "user.fullName")
    ReviewResponse toResponse(Review review);
}
