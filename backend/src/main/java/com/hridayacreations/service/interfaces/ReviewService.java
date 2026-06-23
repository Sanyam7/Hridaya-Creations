package com.hridayacreations.service.interfaces;

import com.hridayacreations.dto.request.CreateReviewRequest;
import com.hridayacreations.dto.request.UpdateReviewRequest;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;

/**
 * Product review operations. Only customers who have purchased a product may review it, at most once.
 */
public interface ReviewService {

    ReviewResponse addReview(CreateReviewRequest request);

    ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request);

    void deleteReview(Long reviewId);

    PagedResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable);
}
