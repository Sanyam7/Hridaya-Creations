package com.hridayacreations.dto.response;

import com.hridayacreations.entity.enums.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Representation of a catalog category.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CategoryResponse", description = "Category details")
public class CategoryResponse {

    private Long id;
    private String categoryName;
    private String description;
    private String imageUrl;
    private CategoryStatus status;
    private Long productCount;
    private Instant createdAt;
    private Instant updatedAt;
}
