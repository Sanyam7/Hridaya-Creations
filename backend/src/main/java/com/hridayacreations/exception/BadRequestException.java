package com.hridayacreations.exception;

/**
 * Thrown for malformed or semantically invalid client input that bean validation cannot express.
 * Mapped to HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
