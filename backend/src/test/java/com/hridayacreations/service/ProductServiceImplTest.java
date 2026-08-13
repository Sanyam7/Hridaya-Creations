package com.hridayacreations.service;

import com.hridayacreations.dto.mapper.ProductMapper;
import com.hridayacreations.dto.request.CreateProductRequest;
import com.hridayacreations.dto.request.ProductColorRequest;
import com.hridayacreations.dto.request.UpdateProductRequest;
import com.hridayacreations.dto.response.ProductResponse;
import com.hridayacreations.entity.Category;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.ProductColor;
import com.hridayacreations.exception.BadRequestException;
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
import com.hridayacreations.service.support.ProductColorResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
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

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        // Built explicitly rather than via @InjectMocks so the colour rules under test run for
        // real — ProductColorResolver is stateless and has no collaborators worth faking.
        productService = new ProductServiceImpl(productRepository, categoryRepository,
                orderItemRepository, cartItemRepository, wishlistRepository, reviewRepository,
                productMapper, auditLogService, new ProductColorResolver());
    }

    private CreateProductRequest validRequest() {
        return CreateProductRequest.builder()
                .name("Magic Mug")
                .categoryId(1L)
                .sellingPrice(new BigDecimal("349.00"))
                .originalPrice(new BigDecimal("499.00"))
                .stockQuantity(10)
                .build();
    }

    private UpdateProductRequest validUpdateRequest() {
        return UpdateProductRequest.builder()
                .name("Magic Mug")
                .categoryId(1L)
                .sellingPrice(new BigDecimal("349.00"))
                .stockQuantity(10)
                .build();
    }

    private ProductColorRequest color(String id, String name, String hexCode) {
        return ProductColorRequest.builder().id(id).name(name).hexCode(hexCode).build();
    }

    /** An already-persisted product in category 1, ready for update tests. */
    private Product existingProduct() {
        Product product = Product.builder()
                .name("Magic Mug")
                .category(Category.builder().build())
                .build();
        product.setId(5L);
        product.getCategory().setId(1L);
        return product;
    }

    private void stubCreate() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(Category.builder().build()));
        when(productRepository.existsBySkuIgnoreCase(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
    }

    private void stubUpdate(Product product) {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Product capturedProduct() {
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        return captor.getValue();
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

    /* ----------------------------- colours ----------------------------- */

    @Test
    void createProduct_withColors_canonicalizesAndDeduplicates() {
        CreateProductRequest request = validRequest();
        request.setHasColors(true);
        request.setColors(List.of(
                color("red", "Totally Wrong Name", "#000000"),  // predefined -> canonicalized
                color("RED", null, null),                        // duplicate -> dropped
                color("black", null, null)));                    // id alone is enough
        stubCreate();

        productService.createProduct(request);

        Product persisted = capturedProduct();
        assertThat(persisted.isHasColors()).isTrue();
        assertThat(persisted.getColors()).extracting(ProductColor::getColorId).containsExactly("red", "black");
        assertThat(persisted.getColors().get(0).getName()).isEqualTo("Red");
        assertThat(persisted.getColors().get(0).getHexCode()).isEqualTo("#E53935");
    }

    @Test
    void createProduct_withoutColors_storesNoneEvenIfSomeWereSubmitted() {
        CreateProductRequest request = validRequest();
        request.setHasColors(false);
        request.setColors(List.of(color("red", null, null)));
        stubCreate();

        productService.createProduct(request);

        Product persisted = capturedProduct();
        assertThat(persisted.isHasColors()).isFalse();
        assertThat(persisted.getColors()).isEmpty();
    }

    @Test
    void createProduct_colorsEnabledButNoneSelected_throws() {
        CreateProductRequest request = validRequest();
        request.setHasColors(true);
        request.setColors(List.of());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(Category.builder().build()));

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least one colour");
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_customColor_requiresNameAndValidHex() {
        CreateProductRequest request = validRequest();
        request.setHasColors(true);
        request.setColors(List.of(color("midnight-blue", "Midnight Blue", "not-a-hex")));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(Category.builder().build()));

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("valid hex code");
    }

    @Test
    void createProduct_customColor_isAcceptedWhenWellFormed() {
        CreateProductRequest request = validRequest();
        request.setHasColors(true);
        request.setColors(List.of(color(null, "Midnight Blue", "#191970")));
        stubCreate();

        productService.createProduct(request);

        assertThat(capturedProduct().getColors()).singleElement()
                .satisfies(c -> {
                    assertThat(c.getColorId()).isEqualTo("midnight-blue");
                    assertThat(c.getName()).isEqualTo("Midnight Blue");
                    assertThat(c.getHexCode()).isEqualTo("#191970");
                });
    }

    @Test
    void updateProduct_replacesColors_soRemovedOnesAreDropped() {
        Product product = existingProduct();
        product.replaceColors(true, List.of(
                ProductColor.builder().colorId("red").name("Red").hexCode("#E53935").build(),
                ProductColor.builder().colorId("black").name("Black").hexCode("#1A1A1A").build(),
                ProductColor.builder().colorId("white").name("White").hexCode("#FFFFFF").build()));
        stubUpdate(product);

        UpdateProductRequest request = validUpdateRequest();
        request.setHasColors(true);
        request.setColors(List.of(color("red", null, null), color("white", null, null)));
        productService.updateProduct(5L, request);

        assertThat(product.getColors()).extracting(ProductColor::getColorId).containsExactly("red", "white");
    }

    @Test
    void updateProduct_disablingColors_clearsThem() {
        Product product = existingProduct();
        product.replaceColors(true, List.of(
                ProductColor.builder().colorId("red").name("Red").hexCode("#E53935").build()));
        stubUpdate(product);

        UpdateProductRequest request = validUpdateRequest();
        request.setHasColors(false);
        request.setColors(List.of());
        productService.updateProduct(5L, request);

        assertThat(product.isHasColors()).isFalse();
        assertThat(product.getColors()).isEmpty();
    }

    @Test
    void updateProduct_withoutColorFields_leavesExistingConfigurationUntouched() {
        Product product = existingProduct();
        product.replaceColors(true, List.of(
                ProductColor.builder().colorId("red").name("Red").hexCode("#E53935").build()));
        stubUpdate(product);

        // A caller that predates the feature sends neither field.
        productService.updateProduct(5L, validUpdateRequest());

        assertThat(product.isHasColors()).isTrue();
        assertThat(product.getColors()).extracting(ProductColor::getColorId).containsExactly("red");
    }

    @Test
    void updateProduct_enablingColorsWithNoneSelected_throws() {
        Product product = existingProduct();
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        UpdateProductRequest request = validUpdateRequest();
        request.setHasColors(true);
        request.setColors(List.of());

        assertThatThrownBy(() -> productService.updateProduct(5L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least one colour");
        verify(productRepository, never()).save(any());
    }

    /* ------------------------------------------------------------------- */

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
