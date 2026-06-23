package com.hridayacreations.service.impl;

import com.hridayacreations.dto.mapper.WishlistMapper;
import com.hridayacreations.dto.request.AddToWishlistRequest;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.dto.response.WishlistResponse;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.User;
import com.hridayacreations.entity.Wishlist;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.repository.UserRepository;
import com.hridayacreations.repository.WishlistRepository;
import com.hridayacreations.security.SecurityUtils;
import com.hridayacreations.service.interfaces.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default wishlist implementation. Adding an already-wishlisted product is idempotent.
 */
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WishlistMapper wishlistMapper;

    @Override
    @Transactional
    public WishlistResponse addToWishlist(AddToWishlistRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        Wishlist wishlist = wishlistRepository.findByUser_IdAndProduct_Id(userId, product.getId())
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    return wishlistRepository.save(Wishlist.builder().user(user).product(product).build());
                });
        return wishlistMapper.toResponse(wishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (!wishlistRepository.existsByUser_IdAndProduct_Id(userId, productId)) {
            throw new ResourceNotFoundException("Wishlist item for product", "id", productId);
        }
        wishlistRepository.deleteByUser_IdAndProduct_Id(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WishlistResponse> getMyWishlist(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Wishlist> page = wishlistRepository.findByUser_Id(userId, pageable);
        return PagedResponse.from(page, wishlistMapper::toResponse);
    }
}
