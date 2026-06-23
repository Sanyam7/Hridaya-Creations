package com.hridayacreations.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger / OpenAPI 3 configuration. Registers a global JWT bearer security scheme so the
 * "Authorize" button in Swagger UI applies a token to secured endpoints.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI hridayaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hridaya Creations API")
                        .description("""
                                REST API for the Hridaya Creations personalized gifting platform.

                                **Authentication**: obtain a JWT via `POST /api/v1/auth/login`, then click
                                *Authorize* and paste the access token. Admin-only endpoints live under
                                `/api/v1/admin/**` and require the `ADMIN` role.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hridaya Creations")
                                .email("support@hridayacreations.com"))
                        .license(new License().name("Proprietary").url("https://hridayacreations.com")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local"),
                        new Server().url("/").description("Current host")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide the JWT access token (without the 'Bearer ' prefix).")));
    }
}
