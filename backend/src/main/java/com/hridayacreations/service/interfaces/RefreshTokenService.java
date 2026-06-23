package com.hridayacreations.service.interfaces;

import com.hridayacreations.entity.RefreshToken;
import com.hridayacreations.entity.User;

/**
 * Manages the lifecycle of persisted refresh tokens (issue, validate, rotate, revoke).
 */
public interface RefreshTokenService {

    /** Issues and persists a new refresh token for the user. */
    RefreshToken create(User user);

    /**
     * Validates that a token exists and is active (not expired, not revoked).
     *
     * @throws com.hridayacreations.exception.TokenRefreshException if invalid
     */
    RefreshToken verifyUsable(String token);

    /** Revokes the supplied token and issues a fresh one for the same user (rotation). */
    RefreshToken rotate(String token);

    /** Revokes a single token; no-op if it does not exist. */
    void revoke(String token);

    /** Revokes every active token belonging to the user (e.g. global logout / password change). */
    void revokeAllForUser(User user);
}
