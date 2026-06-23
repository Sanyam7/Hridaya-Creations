package com.hridayacreations.service.impl;

import com.hridayacreations.dto.mapper.UserMapper;
import com.hridayacreations.dto.request.UpdateProfileRequest;
import com.hridayacreations.dto.request.UpdateUserStatusRequest;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.dto.response.UserResponse;
import com.hridayacreations.entity.User;
import com.hridayacreations.entity.enums.AuditAction;
import com.hridayacreations.exception.DuplicateResourceException;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.UserRepository;
import com.hridayacreations.security.SecurityUtils;
import com.hridayacreations.service.interfaces.AuditLogService;
import com.hridayacreations.service.interfaces.RefreshTokenService;
import com.hridayacreations.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default user implementation covering profile self-service and admin management (listing, lookup
 * and enable/disable).
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        return userMapper.toResponse(getUser(SecurityUtils.getCurrentUserId()));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = getUser(SecurityUtils.getCurrentUserId());

        String newMobile = request.getMobileNumber().trim();
        if (!newMobile.equals(user.getMobileNumber()) && userRepository.existsByMobileNumber(newMobile)) {
            throw new DuplicateResourceException("User", "mobile number", newMobile);
        }
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setMobileNumber(newMobile);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(String keyword, Pageable pageable) {
        Page<User> page = userRepository.search(keyword, pageable);
        return PagedResponse.from(page, userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return userMapper.toResponse(getUser(userId));
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        User user = getUser(userId);
        user.setEnabled(request.getEnabled());
        User saved = userRepository.save(user);

        // Force re-authentication when an account is disabled.
        if (Boolean.FALSE.equals(request.getEnabled())) {
            refreshTokenService.revokeAllForUser(saved);
        }
        auditLogService.log(AuditAction.USER_UPDATED, "User", String.valueOf(userId),
                (Boolean.TRUE.equals(request.getEnabled()) ? "Enabled" : "Disabled") + " user " + saved.getEmail());
        return userMapper.toResponse(saved);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
