package com.hridayacreations.entity.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * The input kind of a customization option. Drives both the control the storefront renders and the
 * validation the backend applies to the submitted value.
 */
public enum CustomizationFieldType {

    /** Single-line free text (name, message, …). Validated against the option's maxLength. */
    TEXT,

    /** Multi-line free text (special instructions). Validated against the option's maxLength. */
    TEXTAREA,

    /** An uploaded image; the submitted value is the stored image URL, not the bytes. */
    IMAGE,

    /** An ISO-8601 local date (yyyy-MM-dd). */
    DATE,

    /** One of the option's fixed choices. */
    SELECT,

    /** One of the colours configured on the product itself; the value is the colour id. */
    COLOR,

    /** A number, optionally bounded by the option's minValue/maxValue. */
    NUMBER,

    /**
     * An explicit yes/no. A required boolean is satisfied by {@code false} just as much as by
     * {@code true} — only the absence of a choice is a missing value.
     */
    BOOLEAN;

    /**
     * The types an admin may pick when creating a custom field. The rest are built-in only:
     * SELECT needs a choice list, IMAGE needs the upload pipeline and COLOR reads the product's
     * own palette, none of which a free-form field definition carries.
     */
    public static final Set<CustomizationFieldType> CUSTOM_FIELD_TYPES =
            Collections.unmodifiableSet(EnumSet.of(TEXT, TEXTAREA, NUMBER, BOOLEAN, DATE));

    public boolean isTextual() {
        return this == TEXT || this == TEXTAREA;
    }
}
