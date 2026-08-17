package com.hridayacreations.service.support;

import com.hridayacreations.entity.enums.CustomizationFieldType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The customization options this application supports, and the only place a new one is registered.
 *
 * <p>Adding an option is a single entry here plus the matching entry in
 * {@code frontend/src/constants/productCustomization.js} — no changes to the entity, the DTOs, the
 * admin form or the storefront renderer, all of which are driven by these definitions.
 *
 * <p>An admin enables a subset of these per product and may override the label and requiredness;
 * everything else (input kind, length limits, allowed choices) is fixed here so the storefront and
 * the server always agree on how a value is rendered and validated.
 */
public final class CustomizationCatalog {

    private CustomizationCatalog() {
        throw new UnsupportedOperationException("Catalog class cannot be instantiated");
    }

    /**
     * A supported customization option.
     *
     * @param key          stable identifier, also the key under which the customer's value is stored
     * @param fieldType    input kind, which decides how the value is validated
     * @param defaultLabel label shown to the customer unless the admin overrides it
     * @param maxLength    length cap for text values; null for non-text fields
     * @param choices      allowed values for {@link CustomizationFieldType#SELECT}; empty otherwise
     * @param displayOrder default position in the customization form
     */
    public record Definition(
            String key,
            CustomizationFieldType fieldType,
            String defaultLabel,
            Integer maxLength,
            List<String> choices,
            int displayOrder) {
    }

    public static final String KEY_CUSTOMER_NAME = "customerName";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_DATE = "date";
    public static final String KEY_COLOR = "color";
    public static final String KEY_FONT = "font";
    public static final String KEY_SIZE = "size";
    public static final String KEY_SPECIAL_INSTRUCTIONS = "specialInstructions";

    private static final List<String> FONT_CHOICES = List.of(
            "Classic Serif", "Script / Cursive", "Bold Modern", "Handwritten", "Elegant Thin");
    private static final List<String> SIZE_CHOICES = List.of("XS", "S", "M", "L", "XL", "XXL");

    private static final Map<String, Definition> BY_KEY = buildCatalog();

    /** Every supported option, in default display order. */
    public static List<Definition> all() {
        return List.copyOf(BY_KEY.values());
    }

    public static Optional<Definition> find(String key) {
        return key == null ? Optional.empty() : Optional.ofNullable(BY_KEY.get(key));
    }

    public static boolean isSupported(String key) {
        return key != null && BY_KEY.containsKey(key);
    }

    private static Map<String, Definition> buildCatalog() {
        Map<String, Definition> catalog = new LinkedHashMap<>();
        add(catalog, new Definition(KEY_CUSTOMER_NAME, CustomizationFieldType.TEXT,
                "Name / Text to Print", 60, List.of(), 1));
        add(catalog, new Definition(KEY_PHOTO, CustomizationFieldType.IMAGE,
                "Photograph / Design", null, List.of(), 2));
        add(catalog, new Definition(KEY_MESSAGE, CustomizationFieldType.TEXT,
                "Personal Message", 200, List.of(), 3));
        add(catalog, new Definition(KEY_DATE, CustomizationFieldType.DATE,
                "Special Date", null, List.of(), 4));
        add(catalog, new Definition(KEY_COLOR, CustomizationFieldType.COLOR,
                "Colour", null, List.of(), 5));
        add(catalog, new Definition(KEY_FONT, CustomizationFieldType.SELECT,
                "Font Style", null, FONT_CHOICES, 6));
        add(catalog, new Definition(KEY_SIZE, CustomizationFieldType.SELECT,
                "Size", null, SIZE_CHOICES, 7));
        add(catalog, new Definition(KEY_SPECIAL_INSTRUCTIONS, CustomizationFieldType.TEXTAREA,
                "Special Instructions", 500, List.of(), 8));
        return Collections.unmodifiableMap(catalog);
    }

    private static void add(Map<String, Definition> catalog, Definition definition) {
        catalog.put(definition.key(), definition);
    }
}
