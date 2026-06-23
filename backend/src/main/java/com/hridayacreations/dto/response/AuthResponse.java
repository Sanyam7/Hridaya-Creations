package com.hridayacreations.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Authentication result returned by login and token-refresh, bundling the JWT pair and the
 * authenticated user's profile.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AuthResponse", description = "Authentication tokens and user profile")
public class AuthResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Opaque refresh token", example = "7f9c2b1a-...")
    private String refreshToken;

    @Builder.Default
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access token lifetime in seconds", example = "3600")
    private long expiresIn;

    @Schema(description = "Authenticated user profile")
    private UserResponse user;
}
