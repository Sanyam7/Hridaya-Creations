package com.hridayacreations.service;

import com.hridayacreations.dto.request.CreateProductRequest;
import com.hridayacreations.dto.request.ProductColorRequest;
import com.hridayacreations.dto.request.UpdateProductRequest;
import com.hridayacreations.dto.response.ProductColorResponse;
import com.hridayacreations.dto.response.ProductResponse;
import com.hridayacreations.entity.Category;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.enums.CategoryStatus;
import com.hridayacreations.entity.enums.ProductStatus;
import com.hridayacreations.repository.CategoryRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.service.interfaces.ProductService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * End-to-end colour behaviour against a real (H2) database: what the admin submits is what gets
 * stored, what gets stored is what the API returns, and colours removed from a product really do
 * disappear from the collection table.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductColorPersistenceTest {

    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        categoryId = categoryRepository.save(Category.builder()
                .categoryName("Colour Test Category")
                .status(CategoryStatus.ACTIVE)
                .build()).getId();
    }

    @Test
    void createWithColors_storesThemAndReturnsThemFromTheApi() {
        Long id = productService.createProduct(request(true, "red", "black", "white")).getId();

        ProductResponse response = reload(id);

        assertThat(response.isHasColors()).isTrue();
        assertThat(response.getColors())
                .extracting(ProductColorResponse::getId, ProductColorResponse::getName,
                        ProductColorResponse::getHexCode)
                .containsExactly(
                        tuple("red", "Red", "#E53935"),
                        tuple("black", "Black", "#1A1A1A"),
                        tuple("white", "White", "#FFFFFF"));
        assertThat(storedColorIds(id)).containsExactly("red", "black", "white");
    }

    @Test
    void createWithoutColors_returnsAnEmptyConfigurationRatherThanNull() {
        Long id = productService.createProduct(request(false)).getId();

        ProductResponse response = reload(id);

        assertThat(response.isHasColors()).isFalse();
        assertThat(response.getColors()).isNotNull().isEmpty();
        assertThat(storedColorIds(id)).isEmpty();
    }

    @Test
    void update_dropsColorsLeftOutOfTheSubmittedList() {
        Long id = productService.createProduct(request(true, "red", "black", "white")).getId();
        flush();

        productService.updateProduct(id, updateRequest(true, "red", "white"));

        assertThat(reload(id).getColors()).extracting(ProductColorResponse::getId)
                .containsExactly("red", "white");
        // The removed colour is gone from the table, not merely hidden.
        assertThat(storedColorIds(id)).containsExactly("red", "white");
    }

    @Test
    void update_addsANewColorToAnExistingSelection() {
        Long id = productService.createProduct(request(true, "red", "black")).getId();
        flush();

        productService.updateProduct(id, updateRequest(true, "red", "black", "white"));

        assertThat(storedColorIds(id)).containsExactly("red", "black", "white");
    }

    @Test
    void update_switchingColorsOff_clearsEveryStoredColor() {
        Long id = productService.createProduct(request(true, "red", "black", "white")).getId();
        flush();

        productService.updateProduct(id, updateRequest(false));

        ProductResponse response = reload(id);
        assertThat(response.isHasColors()).isFalse();
        assertThat(response.getColors()).isEmpty();
        assertThat(storedColorIds(id)).isEmpty();
    }

    @Test
    void update_switchingColorsOn_forAProductThatHadNone() {
        Long id = productService.createProduct(request(false)).getId();
        flush();

        productService.updateProduct(id, updateRequest(true, "green"));

        assertThat(reload(id).isHasColors()).isTrue();
        assertThat(storedColorIds(id)).containsExactly("green");
    }

    @Test
    void productSavedBeforeTheFeature_readsBackAsHavingNoColors() {
        // Mirrors a row written before has_colors/product_colors existed: neither field is touched.
        Product legacy = productRepository.save(Product.builder()
                .name("Legacy Product")
                .category(categoryRepository.findById(categoryId).orElseThrow())
                .sellingPrice(new BigDecimal("199.00"))
                .stockQuantity(5)
                .sku("HC-LEGACY-0001")
                .productStatus(ProductStatus.ACTIVE)
                .build());
        flush();

        ProductResponse response = reload(legacy.getId());

        assertThat(response.isHasColors()).isFalse();
        assertThat(response.getColors()).isNotNull().isEmpty();
    }

    /* ----------------------------------------------------------------- */

    private CreateProductRequest request(boolean hasColors, String... colorIds) {
        return CreateProductRequest.builder()
                .name("Colour Test Product")
                .categoryId(categoryId)
                .sellingPrice(new BigDecimal("299.00"))
                .stockQuantity(10)
                .hasColors(hasColors)
                .colors(colorRequests(colorIds))
                .build();
    }

    private UpdateProductRequest updateRequest(boolean hasColors, String... colorIds) {
        return UpdateProductRequest.builder()
                .name("Colour Test Product")
                .categoryId(categoryId)
                .sellingPrice(new BigDecimal("299.00"))
                .stockQuantity(10)
                .hasColors(hasColors)
                .colors(colorRequests(colorIds))
                .build();
    }

    private List<ProductColorRequest> colorRequests(String... colorIds) {
        return Arrays.stream(colorIds)
                .map(id -> ProductColorRequest.builder().id(id).build())
                .toList();
    }

    /** Forces the pending changes to the database and detaches everything, so reads are real reads. */
    private void flush() {
        entityManager.flush();
        entityManager.clear();
    }

    private ProductResponse reload(Long id) {
        flush();
        return productService.getProductById(id);
    }

    /** Reads the collection table directly — the ground truth for what was actually persisted. */
    private List<String> storedColorIds(Long productId) {
        flush();
        return jdbcTemplate.queryForList(
                "SELECT color_id FROM product_colors WHERE product_id = ? ORDER BY display_order",
                String.class, productId);
    }

}
