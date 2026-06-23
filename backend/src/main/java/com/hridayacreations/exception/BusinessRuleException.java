package com.hridayacreations.exception;

/**
 * Thrown when a request is well-formed but violates a domain/business invariant (e.g. insufficient
 * stock, illegal order-status transition, reviewing a non-purchased product). Mapped to HTTP 422.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
