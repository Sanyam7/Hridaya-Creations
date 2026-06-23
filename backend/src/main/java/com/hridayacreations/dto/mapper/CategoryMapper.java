package com.hridayacreations.dto.mapper;

import com.hridayacreations.dto.request.CreateCategoryRequest;
import com.hridayacreations.dto.request.UpdateCategoryRequest;
import com.hridayacreations.dto.response.CategoryResponse;
import com.hridayacreations.entity.Category;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Maps between {@link Category} and its request/response DTOs. {@code productCount} is enriched by
 * the service layer; {@code status} on creation is defaulted in the service.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "productCount", ignore = true)
    CategoryResponse toResponse(Category category);

    @Mapping(target = "status", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateCategoryRequest request, @MappingTarget Category category);
}
