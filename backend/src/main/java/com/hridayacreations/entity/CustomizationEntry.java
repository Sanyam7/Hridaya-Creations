package com.hridayacreations.entity;

import com.hridayacreations.entity.enums.CustomizationFieldType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One customization value a customer supplied, together with a snapshot of the field it answered.
 *
 * <p>Carrying the label and input kind alongside the value is what keeps a placed order readable
 * after the fact. An admin may later rename a field, change its type or delete it outright; an
 * order that already captured "Lucky Number: 7" still renders as "Lucky Number: 7" instead of
 * degrading to a bare key, or vanishing with the configuration it was validated against.
 *
 * <p>Only the key and value take part in cart-line identity — renaming a field must not split an
 * existing line in two.
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"optionKey", "value"})
public class CustomizationEntry {

    @Column(name = "option_key", nullable = false, length = 60)
    private String optionKey;

    /** The label as shown to the customer when they answered. */
    @Column(name = "label", nullable = false, length = 120)
    private String label;

    /** The input kind as configured when they answered, so the value can still be typed later. */
    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 20)
    private CustomizationFieldType fieldType;

    /**
     * The value in canonical text form ({@code "true"}/{@code "false"} for a boolean, a plain
     * decimal for a number). Typed values are reconstructed from {@link #fieldType} at the API
     * boundary; storing text keeps every field kind in one simple, already-migrated column.
     */
    @Column(name = "option_value", length = 1000)
    private String value;
}
