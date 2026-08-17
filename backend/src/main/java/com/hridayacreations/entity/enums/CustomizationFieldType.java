package com.hridayacreations.entity.enums;

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
    COLOR
}
