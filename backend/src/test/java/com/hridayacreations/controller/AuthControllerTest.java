package com.hridayacreations.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hridayacreations.controller.auth.AuthController;
import com.hridayacreations.dto.request.RegisterRequest;
import com.hridayacreations.dto.response.AuthResponse;
import com.hridayacreations.dto.response.UserResponse;
import com.hridayacreations.security.filters.JwtAuthenticationFilter;
import com.hridayacreations.service.interfaces.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer test for {@link AuthController}: verifies HTTP wiring, the standard response envelope
 * and bean-validation error handling. Security filters are disabled for the slice.
 */
@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void register_validRequest_returns201WithEnvelope() throws Exception {
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(UserResponse.builder().id(1L).email("aarav@example.com").build())
                .build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        RegisterRequest request = RegisterRequest.builder()
                .firstName("Aarav").lastName("Sharma").email("aarav@example.com")
                .mobileNumber("9876543210").password("Secret@123").confirmPassword("Secret@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void register_invalidRequest_returns400() throws Exception {
        RegisterRequest invalid = RegisterRequest.builder()
                .firstName("").lastName("").email("not-an-email")
                .mobileNumber("123").password("weak").confirmPassword("different")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}
