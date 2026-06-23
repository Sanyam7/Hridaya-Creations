package com.hridayacreations.service;

import com.hridayacreations.config.AppProperties;
import com.hridayacreations.dto.mapper.UserMapper;
import com.hridayacreations.dto.request.RegisterRequest;
import com.hridayacreations.dto.response.AuthResponse;
import com.hridayacreations.dto.response.UserResponse;
import com.hridayacreations.entity.RefreshToken;
import com.hridayacreations.entity.Role;
import com.hridayacreations.entity.User;
import com.hridayacreations.entity.enums.RoleName;
import com.hridayacreations.exception.DuplicateResourceException;
import com.hridayacreations.repository.PasswordResetTokenRepository;
import com.hridayacreations.repository.RoleRepository;
import com.hridayacreations.repository.UserRepository;
import com.hridayacreations.security.jwt.JwtTokenProvider;
import com.hridayacreations.service.impl.AuthServiceImpl;
import com.hridayacreations.service.interfaces.AuditLogService;
import com.hridayacreations.service.interfaces.EmailService;
import com.hridayacreations.service.interfaces.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private EmailService emailService;
    @Mock private AuditLogService auditLogService;
    @Mock private UserMapper userMapper;
    @Mock private AppProperties appProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest() {
        return RegisterRequest.builder()
                .firstName("Aarav")
                .lastName("Sharma")
                .email("aarav@example.com")
                .mobileNumber("9876543210")
                .password("Secret@123")
                .confirmPassword("Secret@123")
                .build();
    }

    @Test
    void register_success_issuesTokens() {
        when(userRepository.existsByEmailIgnoreCase("aarav@example.com")).thenReturn(false);
        when(userRepository.existsByMobileNumber("9876543210")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER))
                .thenReturn(Optional.of(Role.builder().name(RoleName.ROLE_USER).build()));
        when(passwordEncoder.encode("Secret@123")).thenReturn("hashed");

        User saved = User.builder()
                .firstName("Aarav").lastName("Sharma").email("aarav@example.com")
                .mobileNumber("9876543210").password("hashed")
                .roles(Set.of(Role.builder().name(RoleName.ROLE_USER).build()))
                .enabled(true).accountNonLocked(true)
                .build();
        saved.setId(11L);
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(3_600_000L);
        when(refreshTokenService.create(saved)).thenReturn(RefreshToken.builder().token("refresh-token").build());
        when(userMapper.toResponse(saved)).thenReturn(UserResponse.builder().id(11L).email("aarav@example.com").build());

        AuthResponse response = authService.register(registerRequest());

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getExpiresIn()).isEqualTo(3600);
        assertThat(response.getUser().getEmail()).isEqualTo("aarav@example.com");
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmailIgnoreCase("aarav@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateMobile_throws() {
        when(userRepository.existsByEmailIgnoreCase("aarav@example.com")).thenReturn(false);
        when(userRepository.existsByMobileNumber("9876543210")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void forgotPassword_unknownEmail_doesNotSendEmail() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        authService.forgotPassword(
                com.hridayacreations.dto.request.ForgotPasswordRequest.builder().email("nobody@example.com").build());

        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
        verify(passwordResetTokenRepository, never()).save(any());
    }
}
