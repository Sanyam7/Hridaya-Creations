package com.hridayacreations.service.impl;

import com.hridayacreations.config.AppProperties;
import com.hridayacreations.dto.request.ChangePasswordRequest;
import com.hridayacreations.dto.request.ForgotPasswordRequest;
import com.hridayacreations.dto.request.LoginRequest;
import com.hridayacreations.dto.request.RefreshTokenRequest;
import com.hridayacreations.dto.request.RegisterRequest;
import com.hridayacreations.dto.request.ResetPasswordRequest;
import com.hridayacreations.dto.response.AuthResponse;
import com.hridayacreations.dto.mapper.UserMapper;
import com.hridayacreations.entity.PasswordResetToken;
import com.hridayacreations.entity.RefreshToken;
import com.hridayacreations.entity.Role;
import com.hridayacreations.entity.User;
import com.hridayacreations.entity.enums.AuditAction;
import com.hridayacreations.entity.enums.RoleName;
import com.hridayacreations.exception.BadRequestException;
import com.hridayacreations.exception.DuplicateResourceException;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.PasswordResetTokenRepository;
import com.hridayacreations.repository.RoleRepository;
import com.hridayacreations.repository.UserRepository;
import com.hridayacreations.security.SecurityUtils;
import com.hridayacreations.security.jwt.JwtTokenProvider;
import com.hridayacreations.security.services.UserPrincipal;
import com.hridayacreations.service.interfaces.AuthService;
import com.hridayacreations.service.interfaces.AuditLogService;
import com.hridayacreations.service.interfaces.EmailService;
import com.hridayacreations.service.interfaces.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

/**
 * Implements registration, authentication, token refresh/rotation, logout and the password
 * lifecycle (forgot / reset / change).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final long RESET_TOKEN_TTL_MINUTES = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new DuplicateResourceException("User", "mobile number", request.getMobileNumber());
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default USER role is not configured"));

        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .mobileNumber(request.getMobileNumber().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of(userRole))
                .build();

        User saved = userRepository.save(user);
        auditLogService.log(AuditAction.USER_CREATED, "User", String.valueOf(saved.getId()),
                "Self-registered account: " + saved.getEmail());
        log.info("Registered new user {}", saved.getEmail());

        return issueTokens(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
        log.info("User {} logged in", user.getEmail());
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken rotated = refreshTokenService.rotate(request.getRefreshToken());
        User user = rotated.getUser();
        String accessToken = jwtTokenProvider.generateAccessToken(UserPrincipal.create(user));
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rotated.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always behave the same way regardless of whether the email exists (no user enumeration).
        userRepository.findByEmailIgnoreCase(request.getEmail()).ifPresent(user -> {
            passwordResetTokenRepository.invalidateAllByUser(user);
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(UUID.randomUUID().toString())
                    .user(user)
                    .expiryDate(Instant.now().plus(RESET_TOKEN_TTL_MINUTES, ChronoUnit.MINUTES))
                    .used(false)
                    .createdAt(Instant.now())
                    .build();
            passwordResetTokenRepository.save(resetToken);

            String resetLink = appProperties.getFrontend().getResetPasswordUrl() + "?token=" + resetToken.getToken();
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
            log.info("Issued password reset token for {}", user.getEmail());
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));
        if (!resetToken.isValid()) {
            throw new BadRequestException("Invalid or expired password reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        refreshTokenService.revokeAllForUser(user);
        log.info("Password reset completed for {}", user.getEmail());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user);
        log.info("Password changed for {}", user.getEmail());
    }

    private AuthResponse issueTokens(User user) {
        UserPrincipal principal = UserPrincipal.create(user);
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        RefreshToken refreshToken = refreshTokenService.create(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }
}
