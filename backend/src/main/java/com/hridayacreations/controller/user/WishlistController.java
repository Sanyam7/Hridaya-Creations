package com.hridayacreations.controller.user;

import com.hridayacreations.constants.AppConstants;
import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.request.AddToWishlistRequest;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.dto.response.WishlistResponse;
import com.hridayacreations.service.interfaces.WishlistService;
import com.hridayacreations.util.PageableBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated wishlist endpoints.
 */
@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Wishlist", description = "Customer wishlist management")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "List the authenticated user's wishlist (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<WishlistResponse>>> getWishlist(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = PageableBuilder.build(page, size, "createdAt", "desc");
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.WISHLIST_FETCHED,
                wishlistService.getMyWishlist(pageable)));
    }

    @PostMapping
    @Operation(summary = "Add a product to the wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> add(@Valid @RequestBody AddToWishlistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.WISHLIST_ITEM_ADDED, wishlistService.addToWishlist(request)));
    }

    @DeleteMapping("/products/{productId}")
    @Operation(summary = "Remove a product from the wishlist")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long productId) {
        wishlistService.removeFromWishlist(productId);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.WISHLIST_ITEM_REMOVED));
    }
}
