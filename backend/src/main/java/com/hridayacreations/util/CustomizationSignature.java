package com.hridayacreations.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * Derives a stable identity for a set of customization values.
 *
 * <p>Two cart lines for the same product are the same line only when their customization matches,
 * so "Water Bottle / John" and "Water Bottle / Sarah" stay separate while adding "John" twice just
 * bumps the quantity. Hashing keeps that identity a fixed-width column no matter how much text the
 * customer entered, which is what lets the database enforce it with a unique constraint.
 */
public final class CustomizationSignature {

    /** Signature of an item with no customization — every readymade line shares it. */
    public static final String NONE = "none";

    /** ASCII unit separator: cannot appear in a submitted value, so it is a safe field delimiter. */
    private static final char SEPARATOR = (char) 0x1F;

    private CustomizationSignature() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * @return a lowercase SHA-256 hex digest of the entries, order-independent, or {@link #NONE}
     *         when there is no customization
     */
    public static String of(Map<String, String> customization) {
        if (customization == null || customization.isEmpty()) {
            return NONE;
        }
        // Sorted so the signature does not depend on the order the client sent the fields in, and
        // delimited so no two distinct maps can canonicalize to the same string.
        StringBuilder canonical = new StringBuilder();
        for (Map.Entry<String, String> entry : new TreeMap<>(customization).entrySet()) {
            canonical.append(entry.getKey()).append(SEPARATOR)
                    .append(entry.getValue() == null ? "" : entry.getValue()).append(SEPARATOR);
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is mandated by the JLS; unreachable on any conformant JVM.
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
