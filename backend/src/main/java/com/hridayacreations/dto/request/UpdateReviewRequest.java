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
 * Updates the authenticated user's existing review for a product.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UpdateReviewRequest", description = "Update review request")
public class UpdateReviewRequest {

    @Schema(example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @Schema(example = "Updated my review after a few weeks of use — still great.")
    @Size(max = 2000, message = "Review text must not exceed 2000 characters")
    private String reviewText;
}
