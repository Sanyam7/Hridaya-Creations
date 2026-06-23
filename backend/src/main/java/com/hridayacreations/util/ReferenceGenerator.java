package com.hridayacreations.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates human-readable, reasonably-unique business references for orders and SKUs. Callers
 * should still enforce uniqueness at persistence time and retry on the rare collision.
 */
public final class ReferenceGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String ALPHANUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private ReferenceGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * @return an order number such as {@code HC-20260622-7F3K9Q}
     */
    public static String generateOrderNumber() {
        return "HC-" + LocalDate.now().format(DATE_FORMAT) + "-" + randomToken(6);
    }

    /**
     * Builds a SKU from a product name prefix and a random suffix, e.g. {@code HC-PER-4B7Q}.
     */
    public static String generateSku(String productName) {
        String prefix = "GEN";
        if (productName != null && !productName.isBlank()) {
            String cleaned = productName.replaceAll("[^A-Za-z]", "").toUpperCase();
            if (!cleaned.isEmpty()) {
                prefix = cleaned.substring(0, Math.min(3, cleaned.length()));
            }
        }
        return "HC-" + prefix + "-" + randomToken(4);
    }

    private static String randomToken(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return builder.toString();
    }
}
