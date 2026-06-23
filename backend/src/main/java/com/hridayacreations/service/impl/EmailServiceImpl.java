package com.hridayacreations.service.impl;

import com.hridayacreations.config.AppProperties;
import com.hridayacreations.service.interfaces.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email implementation backed by {@link JavaMailSender}. When {@code app.mail.enabled} is false (the
 * default for local/dev), the message is logged instead of sent so flows work without an SMTP server.
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final AppProperties appProperties;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public EmailServiceImpl(AppProperties appProperties, ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.appProperties = appProperties;
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    @Async("applicationTaskExecutor")
    public void sendPasswordResetEmail(String toEmail, String recipientName, String resetLink) {
        String subject = "Reset your Hridaya Creations password";
        String body = """
                Hi %s,

                We received a request to reset your Hridaya Creations password.
                Click the link below to choose a new password (valid for 30 minutes):

                %s

                If you did not request this, you can safely ignore this email.

                Warm regards,
                Team Hridaya Creations
                """.formatted(recipientName, resetLink);

        if (!appProperties.getMail().isEnabled()) {
            log.info("[MAIL DISABLED] Password reset email for {} -> link: {}", toEmail, resetLink);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Mail is enabled but no JavaMailSender is configured; skipping email to {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appProperties.getMail().getFrom());
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send password reset email to {}: {}", toEmail, ex.getMessage());
        }
    }
}
