package com.hridayacreations.controller.cart;

import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.request.AddToCartRequest;
import com.hridayacreations.dto.request.UpdateCartItemRequest;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.CartResponse;
import com.hridayacreations.service.interfaces.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated shopping-cart endpoints. Every response includes the full computed price breakdown.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cart", description = "Shopping cart management")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get the authenticated user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.CART_FETCHED, cartService.getMyCart()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add a product to the cart (or increase its quantity)")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.CART_ITEM_ADDED, cartService.addToCart(request)));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Set the quantity of a cart item")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(@PathVariable Long itemId,
                                                                @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.CART_ITEM_UPDATED,
                cartService.updateItemQuantity(itemId, request)));
    }

    @PatchMapping("/items/{itemId}/increase")
    @Operation(summary = "Increase a cart item's quantity by one")
    public ResponseEntity<ApiResponse<CartResponse>> increase(@PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.CART_ITEM_UPDATED,
                cartService.increaseItemQuantity(itemId)));
    }

    @PatchMapping("/items/{itemId}/decrease")
    @Operation(summary = "Decrease a cart item's quantity by one (removes it at zero)")
    public ResponseEntity<ApiResponse<CartResponse>> decrease(@PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.CART_ITEM_UPDATED,
                cartService.decreaseItemQuantity(itemId)));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove an item from the cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.CART_ITEM_REMOVED,
                cartService.removeItem(itemId)));
    }

    @DeleteMapping
    @Operation(summary = "Clear the cart")
    public ResponseEntity<ApiResponse<CartResponse>> clear() {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.CART_CLEARED, cartService.clearCart()));
    }
}
