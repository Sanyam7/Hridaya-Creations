package com.hridayacreations.util;

import com.hridayacreations.entity.CustomizationEntry;
import com.hridayacreations.entity.enums.CustomizationFieldType;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts customization values between the canonical text they are stored as and the typed values
 * the API speaks.
 *
 * <p>Values live in the database as text so that every field kind — a name, a date, a number, a
 * yes/no — shares one simple column and one signature scheme. That is an implementation detail
 * clients should not inherit: JSON out carries a real {@code true} and a real {@code 7}, matching
 * what JSON in accepts, so a boolean never reaches a client as the string {@code "false"} that
 * every language in the world considers truthy.
 */
public final class CustomizationValues {

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    private CustomizationValues() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * @return the value typed according to its field kind: {@link Boolean} for BOOLEAN,
     *         {@link BigDecimal} for NUMBER, the text itself otherwise
     */
    public static Object toApi(CustomizationFieldType fieldType, String storedValue) {
        if (storedValue == null) {
            return null;
        }
        if (fieldType == CustomizationFieldType.BOOLEAN) {
            return TRUE.equals(storedValue);
        }
        if (fieldType == CustomizationFieldType.NUMBER) {
            try {
                return new BigDecimal(storedValue);
            } catch (NumberFormatException ex) {
                // Only reachable if a value predates its field becoming numeric; the raw text is
                // more useful to a client than an error, and the order stays readable.
                return storedValue;
            }
        }
        return storedValue;
    }

    /** The key/value pairs of a snapshot, for signature and comparison purposes. */
    public static Map<String, String> asValueMap(List<CustomizationEntry> entries) {
        Map<String, String> values = new LinkedHashMap<>();
        if (entries != null) {
            for (CustomizationEntry entry : entries) {
                if (entry != null && entry.getOptionKey() != null) {
                    values.put(entry.getOptionKey(), entry.getValue());
                }
            }
        }
        return values;
    }

    /** A display string for a value, used where a single line of text is needed (order notes). */
    public static String toDisplay(CustomizationFieldType fieldType, String storedValue) {
        if (storedValue == null) {
            return "";
        }
        if (fieldType == CustomizationFieldType.BOOLEAN) {
            return TRUE.equals(storedValue) ? "Yes" : "No";
        }
        return storedValue;
    }
}
