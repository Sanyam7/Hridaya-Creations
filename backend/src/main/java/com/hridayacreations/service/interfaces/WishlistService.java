package com.hridayacreations.service.interfaces;

import com.hridayacreations.dto.request.AddToWishlistRequest;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.dto.response.WishlistResponse;
import org.springframework.data.domain.Pageable;

/**
 * Wishlist operations for the authenticated user.
 */
public interface WishlistService {

    WishlistResponse addToWishlist(AddToWishlistRequest request);

    void removeFromWishlist(Long productId);

    PagedResponse<WishlistResponse> getMyWishlist(Pageable pageable);
}
