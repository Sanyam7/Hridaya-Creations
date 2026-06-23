package com.hridayacreations.controller.admin;

import com.hridayacreations.constants.AppConstants;
import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.request.CreateProductRequest;
import com.hridayacreations.dto.request.ProductSearchCriteria;
import com.hridayacreations.dto.request.UpdatePricingRequest;
import com.hridayacreations.dto.request.UpdateProductRequest;
import com.hridayacreations.dto.request.UpdateStockRequest;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.dto.response.ProductResponse;
import com.hridayacreations.entity.enums.ProductStatus;
import com.hridayacreations.service.interfaces.ProductService;
import com.hridayacreations.util.PageableBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin product management: CRUD, availability, pricing and inventory.
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Products", description = "Administrative product management (ADMIN only)")
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a product")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.PRODUCT_CREATED, productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCT_UPDATED,
                productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCT_DELETED));
    }

    @PatchMapping("/{id}/enable")
    @Operation(summary = "Enable a product")
    public ResponseEntity<ApiResponse<ProductResponse>> enable(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCT_ENABLED,
                productService.setProductEnabled(id, true)));
    }

    @PatchMapping("/{id}/disable")
    @Operation(summary = "Disable a product")
    public ResponseEntity<ApiResponse<ProductResponse>> disable(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCT_DISABLED,
                productService.setProductEnabled(id, false)));
    }

    @PatchMapping("/{id}/pricing")
    @Operation(summary = "Update a product's pricing")
    public ResponseEntity<ApiResponse<ProductResponse>> updatePricing(@PathVariable Long id,
                                                                      @Valid @RequestBody UpdatePricingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRICING_UPDATED,
                productService.updatePricing(id, request)));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update a product's stock quantity")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(@PathVariable Long id,
                                                                    @Valid @RequestBody UpdateStockRequest request) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.INVENTORY_UPDATED,
                productService.updateStock(id, request)));
    }

    @GetMapping
    @Operation(summary = "Search products across all statuses (admin, paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .status(status)
                .build();
        Pageable pageable = PageableBuilder.build(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCTS_FETCHED,
                productService.searchProducts(criteria, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id (admin)")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCT_FETCHED,
                productService.getProductById(id)));
    }
}
