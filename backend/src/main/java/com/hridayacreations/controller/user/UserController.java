package com.hridayacreations.controller.user;

import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.request.UpdateProfileRequest;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.UserResponse;
import com.hridayacreations.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated user self-service for profile retrieval and updates.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Profile", description = "Authenticated user profile management")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.USER_FETCHED,
                userService.getCurrentUserProfile()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PROFILE_UPDATED,
                userService.updateProfile(request)));
    }
}
