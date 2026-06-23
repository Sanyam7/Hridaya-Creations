package com.hridayacreations.service.interfaces;

/**
 * Outbound transactional email. Implementations should be non-blocking and must not propagate
 * delivery failures to the caller.
 */
public interface EmailService {

    /**
     * Sends a password-reset email containing the reset link.
     *
     * @param toEmail    recipient address
     * @param recipientName display name for the greeting
     * @param resetLink  fully-formed reset URL including the token
     */
    void sendPasswordResetEmail(String toEmail, String recipientName, String resetLink);
}
