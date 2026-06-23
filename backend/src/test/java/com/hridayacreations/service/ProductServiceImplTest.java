package com.hridayacreations.service;

import com.hridayacreations.dto.mapper.ProductMapper;
import com.hridayacreations.dto.request.CreateProductRequest;
import com.hridayacreations.dto.response.ProductResponse;
import com.hridayacreations.entity.Category;
import com.hridayacreations.entity.Product;
import com.hridayacreations.exception.BusinessRuleException;
import com.hridayacreations.exception.DuplicateResourceException;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.CartItemRepository;
import com.hridayacreations.repository.CategoryRepository;
import com.hridayacreations.repository.OrderItemRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.repository.ReviewRepository;
import com.hridayacreations.repository.WishlistRepository;
import com.hridayacreations.service.impl.ProductServiceImpl;
import com.hridayacreations.service.interfaces.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private WishlistRepository wishlistRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductMapper productMapper;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private ProductServiceImpl productService;

    private CreateProductRequest validRequest() {
        return CreateProductRequest.builder()
                .name("Magic Mug")
                .categoryId(1L)
                .sellingPrice(new BigDecimal("349.00"))
                .originalPrice(new BigDecimal("499.00"))
                .stockQuantity(10)
                .build();
    }

    @Test
    void createProduct_autoGeneratesSkuAndSaves() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(Category.builder().build()));
        when(productRepository.existsBySkuIgnoreCase(anyString())).thenReturn(false);
        Product saved = Product.builder().name("Magic Mug").build();
        saved.setId(7L);
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(ProductResponse.builder().id(7L).name("Magic Mug").build());

        ProductResponse response = productService.createProduct(validRequest());

        assertThat(response.getId()).isEqualTo(7L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_duplicateSku_throws() {
        CreateProductRequest request = validRequest();
        request.setSku("HC-DUP-0001");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(Category.builder().build()));
        when(productRepository.existsBySkuIgnoreCase("HC-DUP-0001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_unknownCategory_throws() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProduct_withExistingOrders_throwsBusinessRule() {
        Product product = Product.builder().name("Magic Mug").build();
        product.setId(3L);
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProduct_Id(3L)).thenReturn(true);

        assertThatThrownBy(() -> productService.deleteProduct(3L))
                .isInstanceOf(BusinessRuleException.class);
        ProductRepository neverDeletes = verify(productRepository, never());
        neverDeletes.delete(any(Product.class));
    }

    @Test
    void deleteProduct_cleansReferencesAndDeletes() {
        Product product = Product.builder().name("Magic Mug").build();
        product.setId(4L);
        when(productRepository.findById(4L)).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProduct_Id(4L)).thenReturn(false);

        productService.deleteProduct(4L);

        verify(cartItemRepository).deleteByProduct_Id(4L);
        verify(wishlistRepository).deleteByProduct_Id(4L);
        verify(reviewRepository).deleteByProduct_Id(4L);
        // Explicit local avoids the delete(T) / delete(Specification<T>) overload ambiguity.
        ProductRepository verifiedRepo = verify(productRepository);
        verifiedRepo.delete(product);
    }
}
