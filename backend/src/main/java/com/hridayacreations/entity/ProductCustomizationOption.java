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

import java.math.BigDecimal;

/**
 * One customization option an admin has switched on for a product.
 *
 * <p>Only enabled options are stored — a row's presence <em>is</em> its enablement, so there is no
 * way to represent a contradictory "present but disabled" state.
 *
 * <p>Two kinds of option share this shape:
 * <ul>
 *   <li><b>Built-in</b> ({@code custom == false}) — the key names an entry in
 *       {@code CustomizationCatalog}, which owns the input kind, choices and hard limits. Only what
 *       the admin may vary per product is stored here.</li>
 *   <li><b>Custom</b> ({@code custom == true}) — an admin-authored field with no catalog entry, so
 *       this row <em>is</em> the whole definition and {@link #fieldType} is authoritative.</li>
 * </ul>
 *
 * <p>Resolving either kind into one uniform spec is {@code CustomizationFieldSpec}'s job; nothing
 * else in the system needs to care which kind it is looking at.
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "optionKey")
public class ProductCustomizationOption {

    /**
     * Stable identifier and the key the customer's value is stored under. Never derived from the
     * label at read time, so renaming a field leaves already-submitted data intelligible.
     */
    @Column(name = "option_key", nullable = false, length = 60)
    private String optionKey;

    /** Label shown to the customer; defaults to the catalog's for a built-in option. */
    @Column(name = "label", nullable = false, length = 120)
    private String label;

    @Column(name = "required", nullable = false)
    private boolean required;

    /**
     * Whether the admin authored this field rather than picking it from the catalog. Stored rather
     * than inferred from a failed catalog lookup, so retiring a catalog entry can never silently
     * reclassify a built-in option as a custom one.
     */
    @Builder.Default
    @Column(name = "is_custom", nullable = false, columnDefinition = "boolean default false")
    private boolean custom = false;

    /** Authoritative for a custom field; null for a built-in one, whose catalog entry decides. */
    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", length = 20)
    private CustomizationFieldType fieldType;

    @Column(name = "placeholder", length = 120)
    private String placeholder;

    /**
     * Character cap for a textual field. For a built-in option this may only tighten the catalog's
     * cap, never loosen it — {@code CustomizationFieldSpec} takes the smaller of the two.
     */
    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "min_value", precision = 18, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 18, scale = 4)
    private BigDecimal maxValue;
}
