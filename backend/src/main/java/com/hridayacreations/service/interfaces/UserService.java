package com.hridayacreations.service.interfaces;

import com.hridayacreations.dto.request.UpdateProfileRequest;
import com.hridayacreations.dto.request.UpdateUserStatusRequest;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

/**
 * User profile self-service and administrative user management.
 */
public interface UserService {

    /* ----- Self-service ----- */

    UserResponse getCurrentUserProfile();

    UserResponse updateProfile(UpdateProfileRequest request);

    /* ----- Admin ----- */

    PagedResponse<UserResponse> getAllUsers(String keyword, Pageable pageable);

    UserResponse getUserById(Long userId);

    UserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request);
}
