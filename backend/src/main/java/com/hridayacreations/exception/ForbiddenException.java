package com.hridayacreations.exception;

/**
 * Thrown when an authenticated principal lacks permission for an operation, or attempts to access a
 * resource owned by another user. Mapped to HTTP 403.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
