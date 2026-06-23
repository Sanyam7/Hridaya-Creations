package com.hridayacreations.exception;

/**
 * Thrown when a refresh token is missing, expired, revoked or otherwise invalid. Mapped to HTTP 401.
 */
public class TokenRefreshException extends RuntimeException {

    public TokenRefreshException(String message) {
        super(message);
    }

    public TokenRefreshException(String token, String message) {
        super("Refresh token [%s] %s".formatted(token, message));
    }
}
