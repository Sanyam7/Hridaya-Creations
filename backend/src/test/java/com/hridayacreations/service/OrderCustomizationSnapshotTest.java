package com.hridayacreations.service;

import com.hridayacreations.dto.request.AddToCartRequest;
import com.hridayacreations.dto.request.CreateOrderRequest;
import com.hridayacreations.dto.request.CreateProductRequest;
import com.hridayacreations.dto.request.ProductCustomizationOptionRequest;
import com.hridayacreations.dto.request.UpdateProductRequest;
import com.hridayacreations.dto.response.CustomizationValueResponse;
import com.hridayacreations.dto.response.OrderItemResponse;
import com.hridayacreations.dto.response.OrderResponse;
import com.hridayacreations.dto.response.ProductResponse;
import com.hridayacreations.entity.Address;
import com.hridayacreations.entity.Category;
import com.hridayacreations.entity.Role;
import com.hridayacreations.entity.User;
import com.hridayacreations.entity.enums.CategoryStatus;
import com.hridayacreations.entity.enums.CustomizationFieldType;
import com.hridayacreations.entity.enums.PaymentMethod;
import com.hridayacreations.entity.enums.ProductType;
import com.hridayacreations.entity.enums.RoleName;
import com.hridayacreations.exception.BadRequestException;
import com.hridayacreations.repository.AddressRepository;
import com.hridayacreations.repository.CategoryRepository;
import com.hridayacreations.repository.RoleRepository;
import com.hridayacreations.repository.UserRepository;
import com.hridayacreations.security.services.UserPrincipal;
import com.hridayacreations.service.interfaces.CartService;
import com.hridayacreations.service.interfaces.OrderService;
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
 * What a placed order remembers, and for how long.
 *
 * <p>An order is a record of what a customer asked for and paid for. Editing a product afterwards
 * is an ordinary admin action — renaming a field, changing its type, deleting it, switching the
 * whole product to readymade — and none of it may reach backwards into orders already placed. This
 * is what makes the order fulfillable at all: the workshop needs the name that was submitted, not
 * whatever the product's configuration happens to say today.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderCustomizationSnapshotTest {

    @Autowired private ProductService productService;
    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private EntityManager entityManager;

    private Long categoryId;
    private Long addressId;

    @BeforeEach
    void setUp() {
        categoryId = categoryRepository.save(Category.builder()
                .categoryName("Snapshot Test Category")
                .status(CategoryStatus.ACTIVE)
                .build()).getId();

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));
        User user = userRepository.save(User.builder()
                .firstName("Order").lastName("Tester")
                .email("order-snapshot@example.com")
                .mobileNumber("9998887771")
                .password("irrelevant")
                .enabled(true).accountNonLocked(true)
                .roles(Set.of(userRole))
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new UserPrincipal(user.getId(), user.getEmail(), "Order Tester", "pw", true, true,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))),
                        null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        addressId = addressRepository.save(Address.builder()
                .user(user)
                .fullName("Order Tester").mobileNumber("9998887771")
                .street("MG Road").city("Bengaluru").state("Karnataka")
                .country("India").pincode("560001")
                .build()).getId();
    }

    @Test
    void aPlacedOrderKeepsItsCustomizationAfterTheFieldIsDeleted() {
        Long productId = productWithLuckyNumber();
        cartService.addToCart(add(productId, Map.of("customerName", "John", "cf_luckyNumber", 7)));
        Long orderId = placeOrder().getId();

        // The admin removes the lucky number field entirely.
        stripToNameOnly(productId);
        entityManager.flush();
        entityManager.clear();

        assertThat(orderService.getMyOrder(orderId).getItems())
                .singleElement()
                .extracting(OrderItemResponse::getCustomization)
                .satisfies(customization -> assertThat(customization)
                        .extracting(CustomizationValueResponse::getKey,
                                CustomizationValueResponse::getLabel,
                                CustomizationValueResponse::getValue)
                        .containsExactly(
                                tuple("customerName", "Name / Text to Print", "John"),
                                tuple("cf_luckyNumber", "Lucky Number", new BigDecimal("7"))));
    }

    @Test
    void aPlacedOrderKeepsTheLabelItWasAnsweredUnderAfterARename() {
        Long productId = productWithLuckyNumber();
        cartService.addToCart(add(productId, Map.of("customerName", "John", "cf_luckyNumber", 7)));
        Long orderId = placeOrder().getId();

        // Same field (same key), new label — the product moves on, the order does not.
        productService.updateProduct(productId, update(List.of(
                builtIn("customerName", true),
                customField("cf_luckyNumber", "Your Number", CustomizationFieldType.NUMBER, false))));
        entityManager.flush();
        entityManager.clear();

        assertThat(productService.getProductById(productId).getCustomizationOptions())
                .extracting("label").contains("Your Number");
        assertThat(orderService.getMyOrder(orderId).getItems().get(0).getCustomization())
                .extracting(CustomizationValueResponse::getLabel)
                .containsExactly("Name / Text to Print", "Lucky Number");
    }

    @Test
    void aPlacedOrderSurvivesTheProductBecomingReadymade() {
        Long productId = productWithLuckyNumber();
        cartService.addToCart(add(productId, Map.of("customerName", "John", "cf_luckyNumber", 7)));
        Long orderId = placeOrder().getId();

        UpdateProductRequest readymade = update(List.of());
        readymade.setProductType(ProductType.READYMADE);
        productService.updateProduct(productId, readymade);
        entityManager.flush();
        entityManager.clear();

        ProductResponse product = productService.getProductById(productId);
        assertThat(product.getProductType()).isEqualTo(ProductType.READYMADE);
        assertThat(product.getCustomizationOptions()).isEmpty();

        // The product no longer takes customization at all...
        assertThatThrownBy(() -> cartService.addToCart(add(productId, Map.of("customerName", "Sarah"))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("readymade product");
        // ...but the order placed while it did still reads in full.
        assertThat(orderService.getMyOrder(orderId).getItems().get(0).getCustomization())
                .hasSize(2)
                .extracting(CustomizationValueResponse::getValue)
                .containsExactly("John", new BigDecimal("7"));
    }

    @Test
    void aBooleanAnsweredNoIsPreservedAsABoolean_notLostAsEmpty() {
        Long productId = createProduct(List.of(
                customField(null, "Gift Wrap?", CustomizationFieldType.BOOLEAN, true)));
        cartService.addToCart(add(productId, Map.of("cf_giftWrap", false)));
        Long orderId = placeOrder().getId();

        entityManager.flush();
        entityManager.clear();

        assertThat(orderService.getMyOrder(orderId).getItems().get(0).getCustomization())
                .singleElement()
                .extracting(CustomizationValueResponse::getKey,
                        CustomizationValueResponse::getFieldType,
                        CustomizationValueResponse::getValue)
                .containsExactly("cf_giftWrap", CustomizationFieldType.BOOLEAN, Boolean.FALSE);
    }

    @Test
    void twoDifferentlyPersonalisedCopiesBecomeTwoOrderLines() {
        Long productId = productWithLuckyNumber();
        cartService.addToCart(add(productId, Map.of("customerName", "John", "cf_luckyNumber", 7)));
        cartService.addToCart(add(productId, Map.of("customerName", "Sarah", "cf_luckyNumber", 10)));

        OrderResponse order = placeOrder();

        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getItems())
                .flatExtracting(OrderItemResponse::getCustomization)
                .filteredOn(value -> "customerName".equals(value.getKey()))
                .extracting(CustomizationValueResponse::getValue)
                .containsExactlyInAnyOrder("John", "Sarah");
    }

    /* ----------------------------------------------------------------- */

    private OrderResponse placeOrder() {
        return orderService.placeOrder(CreateOrderRequest.builder()
                .addressId(addressId)
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .build());
    }

    private AddToCartRequest add(Long productId, Map<String, Object> customization) {
        return AddToCartRequest.builder()
                .productId(productId).quantity(1).customization(customization).build();
    }

    /** Name (required, built-in) plus an admin-authored optional number. */
    private Long productWithLuckyNumber() {
        return createProduct(List.of(
                builtIn("customerName", true),
                customField(null, "Lucky Number", CustomizationFieldType.NUMBER, false)));
    }

    private void stripToNameOnly(Long productId) {
        productService.updateProduct(productId, update(List.of(builtIn("customerName", true))));
    }

    private Long createProduct(List<ProductCustomizationOptionRequest> options) {
        return productService.createProduct(CreateProductRequest.builder()
                .name("Personalised Bottle " + System.nanoTime())
                .categoryId(categoryId)
                .sellingPrice(new BigDecimal("499.00"))
                .stockQuantity(50)
                .productType(ProductType.CUSTOMIZABLE)
                .customizationOptions(options)
                .build()).getId();
    }

    private UpdateProductRequest update(List<ProductCustomizationOptionRequest> options) {
        return UpdateProductRequest.builder()
                .name("Personalised Bottle")
                .categoryId(categoryId)
                .sellingPrice(new BigDecimal("499.00"))
                .stockQuantity(50)
                .productType(options.isEmpty() ? ProductType.READYMADE : ProductType.CUSTOMIZABLE)
                .customizationOptions(options)
                .build();
    }

    private ProductCustomizationOptionRequest builtIn(String key, boolean required) {
        return ProductCustomizationOptionRequest.builder().key(key).required(required).build();
    }

    private ProductCustomizationOptionRequest customField(
            String key, String label, CustomizationFieldType type, boolean required) {
        return ProductCustomizationOptionRequest.builder()
                .key(key).label(label).fieldType(type).required(required).custom(true).build();
    }
}
