package com.hridayacreations.controller.user;

import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.request.CreateReviewRequest;
import com.hridayacreations.dto.request.UpdateReviewRequest;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.ReviewResponse;
import com.hridayacreations.service.interfaces.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated review creation, modification and deletion. Reviewing is restricted to products the
 * user has purchased (enforced in the service layer).
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reviews", description = "Customer product reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Add a review for a purchased product")
    public ResponseEntity<ApiResponse<ReviewResponse>> add(@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.REVIEW_CREATED, reviewService.addReview(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update one of the user's reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(@PathVariable Long id,
                                                              @Valid @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.REVIEW_UPDATED,
                reviewService.updateReview(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete one of the user's reviews")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.REVIEW_DELETED));
    }
}
