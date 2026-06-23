package com.hridayacreations.exception;

/**
 * Thrown when an operation would violate a uniqueness constraint (e.g. duplicate email). Mapped to
 * HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super("%s already exists with %s: '%s'".formatted(resourceName, fieldName, fieldValue));
    }
}
