package com.hridayacreations.controller.product;

import com.hridayacreations.constants.AppConstants;
import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.request.ProductSearchCriteria;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.dto.response.ProductResponse;
import com.hridayacreations.dto.response.ReviewResponse;
import com.hridayacreations.entity.enums.ProductStatus;
import com.hridayacreations.service.interfaces.ProductService;
import com.hridayacreations.service.interfaces.ReviewService;
import com.hridayacreations.util.PageableBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Public product discovery endpoints. Only {@code ACTIVE} products are exposed to customers.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Public product browsing, search and reviews")
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "Search and filter products (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .tag(tag)
                .featured(featured)
                .status(ProductStatus.ACTIVE)
                .build();
        Pageable pageable = PageableBuilder.build(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCTS_FETCHED,
                productService.searchProducts(criteria, pageable)));
    }

    @GetMapping("/featured")
    @Operation(summary = "List featured products")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> featured(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .featured(true)
                .status(ProductStatus.ACTIVE)
                .build();
        Pageable pageable = PageableBuilder.build(page, size, AppConstants.DEFAULT_SORT_BY, "desc");
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCTS_FETCHED,
                productService.searchProducts(criteria, pageable)));
    }

    @GetMapping("/latest")
    @Operation(summary = "List the most recently added products")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> latest(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .status(ProductStatus.ACTIVE)
                .build();
        Pageable pageable = PageableBuilder.build(page, size, "createdAt", "desc");
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCTS_FETCHED,
                productService.searchProducts(criteria, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCT_FETCHED,
                productService.getProductById(id)));
    }

    @GetMapping("/{productId}/reviews")
    @Operation(summary = "List reviews for a product (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> reviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageableBuilder.build(page, size, "createdAt", "desc");
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.REVIEWS_FETCHED,
                reviewService.getProductReviews(productId, pageable)));
    }
}
