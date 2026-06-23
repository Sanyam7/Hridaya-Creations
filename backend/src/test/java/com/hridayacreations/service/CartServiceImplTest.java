package com.hridayacreations.service;

import com.hridayacreations.dto.request.AddToCartRequest;
import com.hridayacreations.dto.response.CartResponse;
import com.hridayacreations.entity.Cart;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.User;
import com.hridayacreations.entity.enums.ProductStatus;
import com.hridayacreations.exception.BusinessRuleException;
import com.hridayacreations.repository.CartItemRepository;
import com.hridayacreations.repository.CartRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.repository.UserRepository;
import com.hridayacreations.security.services.UserPrincipal;
import com.hridayacreations.service.impl.CartServiceImpl;
import com.hridayacreations.service.support.OrderPricingCalculator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderPricingCalculator pricingCalculator;

    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void authenticate() {
        UserPrincipal principal = new UserPrincipal(1L, "u@example.com", "U E", "pw", true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private Product product(int stock, ProductStatus status) {
        Product product = Product.builder()
                .name("Magic Mug").sellingPrice(new BigDecimal("100.00"))
                .stockQuantity(stock).productStatus(status)
                .build();
        product.setId(2L);
        return product;
    }

    @Test
    void addToCart_success_returnsBreakdown() {
        Cart cart = Cart.builder().user(User.builder().build()).build();
        cart.setId(9L);
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product(10, ProductStatus.ACTIVE)));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(pricingCalculator.calculate(any())).thenReturn(new OrderPricingCalculator.PriceBreakdown(
                new BigDecimal("200.00"), new BigDecimal("18"), new BigDecimal("36.00"),
                new BigDecimal("50.00"), new BigDecimal("286.00")));

        CartResponse response = cartService.addToCart(
                AddToCartRequest.builder().productId(2L).quantity(2).build());

        assertThat(response.getTotalItems()).isEqualTo(1);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("286.00");
    }

    @Test
    void addToCart_insufficientStock_throws() {
        Cart cart = Cart.builder().user(User.builder().build()).build();
        cart.setId(9L);
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product(1, ProductStatus.ACTIVE)));

        assertThatThrownBy(() -> cartService.addToCart(
                AddToCartRequest.builder().productId(2L).quantity(5).build()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void addToCart_inactiveProduct_throws() {
        Cart cart = Cart.builder().user(User.builder().build()).build();
        cart.setId(9L);
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product(10, ProductStatus.INACTIVE)));
        lenient().when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        assertThatThrownBy(() -> cartService.addToCart(
                AddToCartRequest.builder().productId(2L).quantity(1).build()))
                .isInstanceOf(BusinessRuleException.class);
    }
}
