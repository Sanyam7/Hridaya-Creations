package com.hridayacreations.service.impl;

import com.hridayacreations.dto.request.AddToCartRequest;
import com.hridayacreations.dto.request.UpdateCartItemRequest;
import com.hridayacreations.dto.response.CartItemResponse;
import com.hridayacreations.dto.response.CartResponse;
import com.hridayacreations.entity.Cart;
import com.hridayacreations.entity.CartItem;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.ProductImage;
import com.hridayacreations.entity.User;
import com.hridayacreations.entity.enums.ProductStatus;
import com.hridayacreations.exception.BusinessRuleException;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.CartItemRepository;
import com.hridayacreations.repository.CartRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.repository.UserRepository;
import com.hridayacreations.security.SecurityUtils;
import com.hridayacreations.service.interfaces.CartService;
import com.hridayacreations.service.support.OrderPricingCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Default cart implementation: maintains one cart per user, validates availability/stock on every
 * mutation, snapshots the current selling price, and returns a fully-computed price breakdown.
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderPricingCalculator pricingCalculator;

    @Override
    @Transactional
    public CartResponse getMyCart() {
        return buildResponse(getOrCreateCart(SecurityUtils.getCurrentUserId()));
    }

    @Override
    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        Cart cart = getOrCreateCart(SecurityUtils.getCurrentUserId());
        Product product = getAvailableProduct(request.getProductId());

        CartItem existing = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        int targetQuantity = (existing != null ? existing.getQuantity() : 0) + request.getQuantity();
        ensureSufficientStock(product, targetQuantity);

        if (existing != null) {
            existing.setQuantity(targetQuantity);
            existing.setUnitPrice(product.getSellingPrice());
        } else {
            CartItem item = CartItem.builder()
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getSellingPrice())
                    .build();
            cart.addItem(item);
        }
        cartRepository.save(cart);
        return buildResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(SecurityUtils.getCurrentUserId());
        CartItem item = getOwnedItem(itemId, cart);
        ensureSufficientStock(item.getProduct(), request.getQuantity());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(item.getProduct().getSellingPrice());
        cartRepository.save(cart);
        return buildResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse increaseItemQuantity(Long itemId) {
        Cart cart = getOrCreateCart(SecurityUtils.getCurrentUserId());
        CartItem item = getOwnedItem(itemId, cart);
        int target = item.getQuantity() + 1;
        ensureSufficientStock(item.getProduct(), target);
        item.setQuantity(target);
        cartRepository.save(cart);
        return buildResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse decreaseItemQuantity(Long itemId) {
        Cart cart = getOrCreateCart(SecurityUtils.getCurrentUserId());
        CartItem item = getOwnedItem(itemId, cart);
        int target = item.getQuantity() - 1;
        if (target < 1) {
            cart.removeItem(item);
        } else {
            item.setQuantity(target);
        }
        cartRepository.save(cart);
        return buildResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long itemId) {
        Cart cart = getOrCreateCart(SecurityUtils.getCurrentUserId());
        CartItem item = getOwnedItem(itemId, cart);
        cart.removeItem(item);
        cartRepository.save(cart);
        return buildResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse clearCart() {
        Cart cart = getOrCreateCart(SecurityUtils.getCurrentUserId());
        cart.clear();
        cartRepository.save(cart);
        return buildResponse(cart);
    }

    /* ----------------------------------------------------------------- */

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUser_Id(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            return cartRepository.save(Cart.builder().user(user).build());
        });
    }

    private CartItem getOwnedItem(Long itemId, Cart cart) {
        return cartItemRepository.findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));
    }

    private Product getAvailableProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        if (product.getProductStatus() != ProductStatus.ACTIVE) {
            throw new BusinessRuleException("Product '%s' is not available for purchase".formatted(product.getName()));
        }
        return product;
    }

    private void ensureSufficientStock(Product product, int requestedQuantity) {
        if (product.getStockQuantity() == null || product.getStockQuantity() < requestedQuantity) {
            throw new BusinessRuleException("Only %d unit(s) of '%s' are available in stock"
                    .formatted(product.getStockQuantity() == null ? 0 : product.getStockQuantity(),
                            product.getName()));
        }
    }

    private CartResponse buildResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal subtotal = cart.getItems().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        OrderPricingCalculator.PriceBreakdown breakdown = pricingCalculator.calculate(subtotal);

        int totalQuantity = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();

        return CartResponse.builder()
                .id(cart.getId())
                .items(itemResponses)
                .totalItems(itemResponses.size())
                .totalQuantity(totalQuantity)
                .subtotal(breakdown.subtotal())
                .gstAmount(breakdown.gstAmount())
                .gstPercentage(breakdown.gstPercentage())
                .deliveryCharge(breakdown.deliveryCharge())
                .totalAmount(breakdown.totalAmount())
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        Product product = item.getProduct();
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .sku(product.getSku())
                .imageUrl(primaryImageUrl(product))
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .lineTotal(item.getLineTotal())
                .inStock(product.isInStock())
                .availableStock(product.getStockQuantity())
                .build();
    }

    private String primaryImageUrl(Product product) {
        return product.getImages().stream()
                .filter(ProductImage::isPrimaryImage)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElseGet(() -> product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl());
    }
}
