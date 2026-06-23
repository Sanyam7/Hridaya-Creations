package com.hridayacreations.config;

import com.hridayacreations.entity.Category;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.Role;
import com.hridayacreations.entity.User;
import com.hridayacreations.entity.enums.CategoryStatus;
import com.hridayacreations.entity.enums.ProductStatus;
import com.hridayacreations.entity.enums.RoleName;
import com.hridayacreations.repository.CategoryRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.repository.RoleRepository;
import com.hridayacreations.repository.UserRepository;
import com.hridayacreations.util.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Idempotent bootstrap data: ensures the RBAC roles and the default administrator exist on every
 * startup, and optionally seeds a sample catalog (categories + products) for non-production use.
 */
@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedAdmin();
        if (appProperties.getSeed().isSampleDataEnabled()) {
            seedSampleCatalog();
        }
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                log.info("Seeding role {}", roleName);
                return roleRepository.save(Role.builder()
                        .name(roleName)
                        .description(roleName == RoleName.ROLE_ADMIN
                                ? "Platform administrator with full management access"
                                : "Standard customer")
                        .build());
            });
        }
    }

    private void seedAdmin() {
        AppProperties.Admin admin = appProperties.getAdmin();
        if (userRepository.existsByEmailIgnoreCase(admin.getEmail())) {
            return;
        }
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN must be seeded before the admin user"));

        User adminUser = User.builder()
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .email(admin.getEmail().toLowerCase())
                .mobileNumber(admin.getMobile())
                .password(passwordEncoder.encode(admin.getPassword()))
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of(adminRole))
                .build();
        userRepository.save(adminUser);
        log.info("Seeded default administrator account: {}", admin.getEmail());
    }

    private void seedSampleCatalog() {
        if (categoryRepository.count() > 0 || productRepository.count() > 0) {
            return;
        }
        log.info("Seeding sample catalog (categories + products)");

        Category mugs = category("Personalized Mugs", "Custom printed mugs with names, photos and messages");
        Category frames = category("Photo Frames", "Personalized photo frames for every memory");
        Category cushions = category("Cushions", "Custom printed cushions and pillows");
        Category keychains = category("Keychains", "Personalized name and photo keychains");
        Category giftsHim = category("Gifts For Him", "Thoughtful personalized gifts for him");
        Category giftsHer = category("Gifts For Her", "Thoughtful personalized gifts for her");
        Category anniversary = category("Anniversary Gifts", "Celebrate love with personalized anniversary gifts");
        Category birthday = category("Birthday Gifts", "Make birthdays special with custom gifts");

        categoryRepository.saveAll(List.of(mugs, frames, cushions, keychains,
                giftsHim, giftsHer, anniversary, birthday));

        productRepository.saveAll(List.of(
                product("Personalized Photo Mug", mugs,
                        "A classic ceramic mug printed with your favourite photo and a custom message.",
                        "Custom photo ceramic mug", "299.00", "399.00", 120, true, true,
                        Set.of("mug", "photo", "ceramic", "birthday")),
                product("Magic Heat Reveal Mug", mugs,
                        "A heat-sensitive magic mug that reveals your photo when filled with a hot beverage.",
                        "Heat-reveal magic mug", "449.00", "599.00", 80, true, true,
                        Set.of("mug", "magic", "photo")),
                product("LED Personalized Photo Frame", frames,
                        "An elegant LED-lit frame customised with your photo and name.",
                        "LED-lit personalized frame", "899.00", "1199.00", 50, true, true,
                        Set.of("frame", "led", "photo", "anniversary")),
                product("Wooden Engraved Photo Frame", frames,
                        "A premium wooden frame laser-engraved with your photo and a message.",
                        "Laser-engraved wooden frame", "749.00", "999.00", 60, false, true,
                        Set.of("frame", "wood", "engraved")),
                product("Custom Printed Cushion", cushions,
                        "A soft cushion printed with your photo and message — perfect for gifting.",
                        "Custom photo cushion", "499.00", "699.00", 100, true, true,
                        Set.of("cushion", "photo", "home")),
                product("Personalized Name Keychain", keychains,
                        "A durable metal keychain engraved with a name of your choice.",
                        "Engraved name keychain", "149.00", "249.00", 200, false, true,
                        Set.of("keychain", "name", "engraved")),
                product("Anniversary Photo Collage Frame", anniversary,
                        "A multi-photo collage frame to celebrate years of togetherness.",
                        "Multi-photo collage frame", "1099.00", "1499.00", 40, true, true,
                        Set.of("anniversary", "collage", "frame")),
                product("Birthday Surprise Gift Box", birthday,
                        "A curated personalized gift box to make birthdays unforgettable.",
                        "Personalized birthday gift box", "1299.00", "1699.00", 35, true, true,
                        Set.of("birthday", "giftbox", "surprise"))
        ));
        log.info("Sample catalog seeded: 8 categories, 8 products");
    }

    private Category category(String name, String description) {
        return Category.builder()
                .categoryName(name)
                .description(description)
                .status(CategoryStatus.ACTIVE)
                .build();
    }

    private Product product(String name, Category category, String description, String shortDescription,
                            String sellingPrice, String originalPrice, int stock, boolean featured,
                            boolean customizable, Set<String> tags) {
        return Product.builder()
                .name(name)
                .category(category)
                .description(description)
                .shortDescription(shortDescription)
                .sellingPrice(new BigDecimal(sellingPrice))
                .originalPrice(new BigDecimal(originalPrice))
                .stockQuantity(stock)
                .sku(ReferenceGenerator.generateSku(name))
                .productStatus(ProductStatus.ACTIVE)
                .featured(featured)
                .customizable(customizable)
                .tags(new java.util.HashSet<>(tags))
                .averageRating(BigDecimal.ZERO)
                .ratingCount(0)
                .build();
    }
}
