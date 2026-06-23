package com.hridayacreations.service.interfaces;

import com.hridayacreations.dto.request.AddToCartRequest;
import com.hridayacreations.dto.request.UpdateCartItemRequest;
import com.hridayacreations.dto.response.CartResponse;

/**
 * Shopping cart operations for the authenticated user.
 */
public interface CartService {

    CartResponse getMyCart();

    CartResponse addToCart(AddToCartRequest request);

    CartResponse updateItemQuantity(Long itemId, UpdateCartItemRequest request);

    CartResponse increaseItemQuantity(Long itemId);

    CartResponse decreaseItemQuantity(Long itemId);

    CartResponse removeItem(Long itemId);

    CartResponse clearCart();
}
