package com.hridayacreations.controller.auth;

import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.request.ChangePasswordRequest;
import com.hridayacreations.dto.request.ForgotPasswordRequest;
import com.hridayacreations.dto.request.LoginRequest;
import com.hridayacreations.dto.request.RefreshTokenRequest;
import com.hridayacreations.dto.request.RegisterRequest;
import com.hridayacreations.dto.request.ResetPasswordRequest;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.AuthResponse;
import com.hridayacreations.service.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication and credential-management endpoints. All endpoints are public except
 * {@code change-password}, which requires an authenticated user.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, token refresh and password management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new customer account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.REGISTER_SUCCESS, response));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and obtain access & refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.LOGIN_SUCCESS, authService.login(request)));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Exchange a refresh token for a new access token (with rotation)")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(MessageConstants.TOKEN_REFRESH_SUCCESS, authService.refreshToken(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke a refresh token (logout)")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.LOGOUT_SUCCESS));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset link")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PASSWORD_RESET_LINK_SENT));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using a valid reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PASSWORD_RESET_SUCCESS));
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change the authenticated user's password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PASSWORD_CHANGED));
    }
}
