package com.hridayacreations.service;

import com.hridayacreations.dto.request.AddToCartRequest;
import com.hridayacreations.dto.request.CreateProductRequest;
import com.hridayacreations.dto.request.ProductColorRequest;
import com.hridayacreations.dto.request.ProductCustomizationOptionRequest;
import com.hridayacreations.dto.response.CartItemResponse;
import com.hridayacreations.dto.response.CartResponse;
import com.hridayacreations.dto.response.CustomizationValueResponse;
import com.hridayacreations.dto.response.ProductCustomizationOptionResponse;
import com.hridayacreations.dto.response.ProductResponse;
import com.hridayacreations.entity.Category;
import com.hridayacreations.entity.Role;
import com.hridayacreations.entity.User;
import com.hridayacreations.entity.enums.CategoryStatus;
import com.hridayacreations.entity.enums.CustomizationFieldType;
import com.hridayacreations.entity.enums.ProductType;
import com.hridayacreations.entity.enums.RoleName;
import com.hridayacreations.exception.BadRequestException;
import com.hridayacreations.repository.CategoryRepository;
import com.hridayacreations.repository.RoleRepository;
import com.hridayacreations.repository.UserRepository;
import com.hridayacreations.security.services.UserPrincipal;
import com.hridayacreations.service.interfaces.CartService;
import com.hridayacreations.service.interfaces.ProductService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * End-to-end customization behaviour against a real (H2) database: what the admin enabled is what
 * the customer may submit, personalised lines stay distinct in the cart, and a hand-crafted request
 * cannot smuggle in an option the admin never switched on.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartCustomizationPersistenceTest {

    @Autowired private ProductService productService;
    @Autowired private CartService cartService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private EntityManager entityManager;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        categoryId = categoryRepository.save(Category.builder()
                .categoryName("Customization Test Category")
                .status(CategoryStatus.ACTIVE)
                .build()).getId();

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));
        User user = userRepository.save(User.builder()
                .firstName("Cart").lastName("Tester")
                .email("cart-customization@example.com")
                .mobileNumber("9998887770")
                .password("irrelevant")
                .enabled(true).accountNonLocked(true)
                .roles(Set.of(userRole))
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new UserPrincipal(user.getId(), user.getEmail(), "Cart Tester", "pw", true, true,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))),
                        null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @Test
    void differentPersonalisationsOfTheSameProductStayAsSeparateLines() {
        Long productId = customizableProduct();

        cartService.addToCart(add(productId, 1, Map.of("customerName", "John")));
        CartResponse cart = cartService.addToCart(add(productId, 1, Map.of("customerName", "Sarah")));

        assertThat(cart.getItems()).hasSize(2);
        assertThat(cart.getItems()).extracting(i -> valueOf(i, "customerName"))
                .containsExactlyInAnyOrder("John", "Sarah");
        assertThat(cart.getTotalQuantity()).isEqualTo(2);
    }

    @Test
    void anIdenticalPersonalisationMergesIntoAQuantityBump() {
        Long productId = customizableProduct();

        cartService.addToCart(add(productId, 1, Map.of("customerName", "John")));
        CartResponse cart = cartService.addToCart(add(productId, 2, Map.of("customerName", "John")));

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void fieldOrderDoesNotAffectLineIdentity() {
        Long productId = customizableProduct();
        // Same values, built in different insertion orders: still one line.
        cartService.addToCart(add(productId, 1, Map.of("customerName", "John", "message", "Hi")));
        CartResponse cart = cartService.addToCart(add(productId, 1, Map.of("message", "Hi", "customerName", "John")));

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void customizationSurvivesTheRoundTripToTheDatabase() {
        Long productId = customizableProduct();
        cartService.addToCart(add(productId, 1, Map.of("customerName", "John", "message", "Happy Birthday!")));

        entityManager.flush();
        entityManager.clear();

        CartItemResponse line = cartService.getMyCart().getItems().get(0);
        assertThat(line.getCustomization())
                .extracting(CustomizationValueResponse::getKey, CustomizationValueResponse::getLabel,
                        CustomizationValueResponse::getValue)
                .containsExactly(
                        tuple("customerName", "Name / Text to Print", "John"),
                        tuple("message", "Personal Message", "Happy Birthday!"));
        assertThat(line.getProductType()).isEqualTo(ProductType.CUSTOMIZABLE);
    }

    @Test
    void theProductApiDescribesEveryFieldTheStorefrontMustRender() {
        Long productId = customizableProduct();
        entityManager.flush();
        entityManager.clear();

        ProductResponse product = productService.getProductById(productId);

        assertThat(product.getProductType()).isEqualTo(ProductType.CUSTOMIZABLE);
        assertThat(product.isCustomizable()).isTrue();   // derived, for older clients
        assertThat(product.getCustomizationOptions())
                .extracting(ProductCustomizationOptionResponse::getKey,
                        ProductCustomizationOptionResponse::getLabel,
                        ProductCustomizationOptionResponse::isRequired,
                        ProductCustomizationOptionResponse::getFieldType,
                        ProductCustomizationOptionResponse::getMaxLength)
                .containsExactly(
                        tuple("customerName", "Name / Text to Print", true, CustomizationFieldType.TEXT, 60),
                        tuple("message", "Personal Message", false, CustomizationFieldType.TEXT, 200));
    }

    @Test
    void aReadymadeProductAdvertisesNoOptions() {
        Long productId = readymadeProduct();
        entityManager.flush();
        entityManager.clear();

        ProductResponse product = productService.getProductById(productId);

        assertThat(product.getProductType()).isEqualTo(ProductType.READYMADE);
        assertThat(product.isCustomizable()).isFalse();
        assertThat(product.getCustomizationOptions()).isNotNull().isEmpty();
    }

    /* ------------------------- security boundary ------------------------- */

    @Test
    void anOptionTheAdminDidNotEnableIsRejected() {
        // Only customerName is enabled; a hand-crafted request adds a photo anyway.
        Long productId = customizableProduct();

        assertThatThrownBy(() -> cartService.addToCart(
                add(productId, 1, Map.of("customerName", "John", "photo", "/api/v1/images/" + "a".repeat(32)))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not a customization option offered");
    }

    @Test
    void aReadymadeProductRejectsAnyCustomization() {
        Long productId = readymadeProduct();

        assertThatThrownBy(() -> cartService.addToCart(add(productId, 1, Map.of("customerName", "John"))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("readymade product");
    }

    @Test
    void aReadymadeProductAddsStraightToTheCart() {
        Long productId = readymadeProduct();

        CartResponse cart = cartService.addToCart(add(productId, 2, null));

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(cart.getItems().get(0).getCustomization()).isEmpty();
        assertThat(cart.getItems().get(0).getProductType()).isEqualTo(ProductType.READYMADE);
    }

    @Test
    void aMissingRequiredValueIsRejected() {
        Long productId = customizableProduct();

        assertThatThrownBy(() -> cartService.addToCart(add(productId, 1, Map.of())))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("is required");
    }

    @Test
    void anOverlongTextValueIsRejected() {
        Long productId = customizableProduct();

        assertThatThrownBy(() -> cartService.addToCart(
                add(productId, 1, Map.of("customerName", "x".repeat(61)))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must not exceed 60 characters");
    }

    @Test
    void anImageValueMustBeAUrlThisSiteIssued() {
        Long productId = productWithPhoto();

        assertThatThrownBy(() -> cartService.addToCart(
                add(productId, 1, Map.of("photo", "https://evil.example.com/tracker.png"))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("uploaded through this site");
    }

    @Test
    void aColourNotOfferedByTheProductIsRejected() {
        Long productId = productWithColour();

        assertThatThrownBy(() -> cartService.addToCart(add(productId, 1, Map.of("color", "purple"))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not a colour offered");
    }

    @Test
    void aColourTheProductOffersIsAccepted() {
        Long productId = productWithColour();

        CartResponse cart = cartService.addToCart(add(productId, 1, Map.of("color", "red")));

        assertThat(valueOf(cart.getItems().get(0), "color")).isEqualTo("red");
    }

    /* ----------------------------------------------------------------- */

    /** The value of one field on a cart line, or null when the line has no such field. */
    private static Object valueOf(CartItemResponse line, String key) {
        return line.getCustomization().stream()
                .filter(value -> key.equals(value.getKey()))
                .map(CustomizationValueResponse::getValue)
                .findFirst().orElse(null);
    }

    private AddToCartRequest add(Long productId, int quantity, Map<String, Object> customization) {
        return AddToCartRequest.builder()
                .productId(productId).quantity(quantity).customization(customization).build();
    }

    /** Name required, message optional — nothing else offered. */
    private Long customizableProduct() {
        return createProduct("Personalised Bottle", ProductType.CUSTOMIZABLE, false, List.of(),
                List.of(optionRequest("customerName", true), optionRequest("message", false)));
    }

    private Long productWithPhoto() {
        return createProduct("Photo Frame", ProductType.CUSTOMIZABLE, false, List.of(),
                List.of(optionRequest("photo", false)));
    }

    private Long productWithColour() {
        return createProduct("Colour Mug", ProductType.CUSTOMIZABLE, true,
                List.of(ProductColorRequest.builder().id("red").build()),
                List.of(optionRequest("color", false)));
    }

    private Long readymadeProduct() {
        return createProduct("Standard Mug", ProductType.READYMADE, false, List.of(), List.of());
    }

    private Long createProduct(String name, ProductType type, boolean hasColors,
                               List<ProductColorRequest> colors,
                               List<ProductCustomizationOptionRequest> options) {
        return productService.createProduct(CreateProductRequest.builder()
                .name(name)
                .categoryId(categoryId)
                .sellingPrice(new BigDecimal("499.00"))
                .stockQuantity(50)
                .productType(type)
                .customizationOptions(options)
                .hasColors(hasColors)
                .colors(colors)
                .build()).getId();
    }

    private ProductCustomizationOptionRequest optionRequest(String key, boolean required) {
        return ProductCustomizationOptionRequest.builder().key(key).required(required).build();
    }
}
