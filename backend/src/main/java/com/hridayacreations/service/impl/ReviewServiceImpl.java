package com.hridayacreations.service.impl;

import com.hridayacreations.dto.mapper.ReviewMapper;
import com.hridayacreations.dto.request.CreateReviewRequest;
import com.hridayacreations.dto.request.UpdateReviewRequest;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.dto.response.ReviewResponse;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.Review;
import com.hridayacreations.entity.User;
import com.hridayacreations.exception.BusinessRuleException;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.OrderRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.repository.ReviewRepository;
import com.hridayacreations.repository.UserRepository;
import com.hridayacreations.security.SecurityUtils;
import com.hridayacreations.service.interfaces.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Default review implementation. Enforces the purchase-before-review rule and a single review per
 * user/product, and keeps the product's aggregate rating in sync.
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse addReview(CreateReviewRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!orderRepository.hasUserPurchasedProduct(userId, product.getId())) {
            throw new BusinessRuleException("You can only review products you have purchased");
        }
        if (reviewRepository.existsByUser_IdAndProduct_Id(userId, product.getId())) {
            throw new BusinessRuleException("You have already reviewed this product. Edit your existing review instead.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .reviewText(request.getReviewText())
                .build();
        Review saved = reviewRepository.save(review);

        recalculateProductRating(product);
        return reviewMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Review review = reviewRepository.findByIdAndUser_Id(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        Review saved = reviewRepository.save(review);

        recalculateProductRating(review.getProduct());
        return reviewMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Review review = reviewRepository.findByIdAndUser_Id(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        Product product = review.getProduct();
        reviewRepository.delete(review);
        recalculateProductRating(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        Page<Review> page = reviewRepository.findByProduct_Id(productId, pageable);
        return PagedResponse.from(page, reviewMapper::toResponse);
    }

    private void recalculateProductRating(Product product) {
        Double average = reviewRepository.findAverageRatingByProductId(product.getId());
        long count = reviewRepository.countByProduct_Id(product.getId());
        BigDecimal rounded = average == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
        product.setAverageRating(rounded);
        product.setRatingCount((int) count);
        productRepository.save(product);
    }
}
