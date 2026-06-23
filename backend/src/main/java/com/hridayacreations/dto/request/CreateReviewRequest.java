package com.hridayacreations.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Creates a review for a purchased product.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreateReviewRequest", description = "Create review request")
public class CreateReviewRequest {

    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Product id is required")
    private Long productId;

    @Schema(example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @Schema(example = "Beautiful product, exactly as pictured. Highly recommended!")
    @Size(max = 2000, message = "Review text must not exceed 2000 characters")
    private String reviewText;
}
