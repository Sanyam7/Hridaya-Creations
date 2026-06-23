package com.hridayacreations.service;

import com.hridayacreations.dto.mapper.ReviewMapper;
import com.hridayacreations.dto.request.CreateReviewRequest;
import com.hridayacreations.entity.Product;
import com.hridayacreations.exception.BusinessRuleException;
import com.hridayacreations.repository.OrderRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.repository.ReviewRepository;
import com.hridayacreations.repository.UserRepository;
import com.hridayacreations.security.services.UserPrincipal;
import com.hridayacreations.service.impl.ReviewServiceImpl;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

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

    private Product product() {
        Product product = Product.builder().name("Magic Mug").build();
        product.setId(2L);
        return product;
    }

    @Test
    void addReview_notPurchased_throws() {
        when(productRepository.findById(2L)).thenReturn(Optional.of(product()));
        when(orderRepository.hasUserPurchasedProduct(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.addReview(
                CreateReviewRequest.builder().productId(2L).rating(5).build()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void addReview_alreadyReviewed_throws() {
        when(productRepository.findById(2L)).thenReturn(Optional.of(product()));
        when(orderRepository.hasUserPurchasedProduct(1L, 2L)).thenReturn(true);
        when(reviewRepository.existsByUser_IdAndProduct_Id(1L, 2L)).thenReturn(true);
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.addReview(
                CreateReviewRequest.builder().productId(2L).rating(4).build()))
                .isInstanceOf(BusinessRuleException.class);
    }
}
