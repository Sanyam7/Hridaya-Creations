package com.hridayacreations.exception;

/**
 * Thrown when authentication is missing or invalid. Mapped to HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
