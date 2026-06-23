package com.hridayacreations.service.interfaces;

import com.hridayacreations.dto.request.ChangePasswordRequest;
import com.hridayacreations.dto.request.ForgotPasswordRequest;
import com.hridayacreations.dto.request.LoginRequest;
import com.hridayacreations.dto.request.RefreshTokenRequest;
import com.hridayacreations.dto.request.RegisterRequest;
import com.hridayacreations.dto.request.ResetPasswordRequest;
import com.hridayacreations.dto.response.AuthResponse;

/**
 * Authentication and credential-management operations.
 */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}
