package com.hridayacreations.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Representation of a product review.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ReviewResponse", description = "Product review details")
public class ReviewResponse {

    private Long id;
    private Long productId;
    private Long userId;
    private String reviewerName;
    private Integer rating;
    private String reviewText;
    private Instant createdAt;
    private Instant updatedAt;
}
