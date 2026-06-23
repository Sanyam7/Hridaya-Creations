package com.hridayacreations.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strongly-typed binding of the {@code app.*} configuration tree. Centralises all tunable
 * application settings (JWT, CORS, Cloudinary, default admin, order pricing, mail, seeding).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NestedConfigurationProperty
    private Jwt jwt = new Jwt();

    @NestedConfigurationProperty
    private Cors cors = new Cors();

    @NestedConfigurationProperty
    private Cloudinary cloudinary = new Cloudinary();

    @NestedConfigurationProperty
    private Admin admin = new Admin();

    @NestedConfigurationProperty
    private Order order = new Order();

    @NestedConfigurationProperty
    private Mail mail = new Mail();

    @NestedConfigurationProperty
    private Frontend frontend = new Frontend();

    @NestedConfigurationProperty
    private Seed seed = new Seed();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs = 3_600_000L;
        private long refreshTokenExpirationMs = 604_800_000L;
        private String issuer = "hridaya-creations";
        private String tokenPrefix = "Bearer ";
        private String header = "Authorization";
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:3000");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private List<String> exposedHeaders = List.of("Authorization");
        private boolean allowCredentials = true;
        private long maxAge = 3600L;
    }

    @Getter
    @Setter
    public static class Cloudinary {
        private String cloudName;
        private String apiKey;
        private String apiSecret;
        private String folder = "hridaya-creations";
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class Admin {
        private String email;
        private String password;
        private String firstName = "Hridaya";
        private String lastName = "Admin";
        private String mobile = "9999999999";
    }

    @Getter
    @Setter
    public static class Order {
        private BigDecimal gstPercentage = new BigDecimal("18");
        private BigDecimal deliveryCharge = new BigDecimal("50");
        private BigDecimal freeDeliveryThreshold = new BigDecimal("500");
    }

    @Getter
    @Setter
    public static class Mail {
        private String from = "no-reply@hridayacreations.com";
        private boolean enabled = false;
    }

    @Getter
    @Setter
    public static class Frontend {
        private String resetPasswordUrl = "http://localhost:3000/reset-password";
    }

    @Getter
    @Setter
    public static class Seed {
        private boolean sampleDataEnabled = true;
    }
}
