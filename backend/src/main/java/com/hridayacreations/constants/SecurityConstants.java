package com.hridayacreations.constants;

/**
 * Security-related constants: public URL patterns and role/authority names.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    public static final String AUTHORITY_ADMIN = "ROLE_ADMIN";
    public static final String AUTHORITY_USER = "ROLE_USER";

    /** Endpoints that never require authentication, for any HTTP method. */
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/health/**",
            "/actuator/info"
    };

    /** Read-only catalog endpoints that are public for GET requests only. */
    public static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/v1/products/**",
            "/api/v1/categories/**"
    };

    /** Endpoints restricted to ADMIN role. */
    public static final String ADMIN_ENDPOINTS = "/api/v1/admin/**";
}
