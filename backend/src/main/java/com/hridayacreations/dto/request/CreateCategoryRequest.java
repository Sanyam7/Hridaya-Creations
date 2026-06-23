package com.hridayacreations.dto.request;

import com.hridayacreations.entity.enums.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

/**
 * Admin payload to create a catalog category.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreateCategoryRequest", description = "Create category request")
public class CreateCategoryRequest {

    @Schema(example = "Personalized Mugs", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Category name is required")
    @Size(max = 120, message = "Category name must not exceed 120 characters")
    private String categoryName;

    @Schema(example = "Custom printed mugs with names, photos and messages")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Schema(example = "https://res.cloudinary.com/demo/image/upload/mugs.jpg")
    @URL(message = "Image URL must be a valid URL")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Schema(description = "Defaults to ACTIVE when omitted", example = "ACTIVE")
    private CategoryStatus status;
}
