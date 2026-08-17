package com.hridayacreations.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One customization option an admin has switched on for a product.
 *
 * <p>Only enabled options are stored — a row's presence <em>is</em> its enablement, so there is no
 * way to represent a contradictory "present but disabled" state. The input kind, length limits and
 * allowed choices live in {@code CustomizationCatalog} keyed by {@link #optionKey}; only what the
 * admin can vary per product (label and requiredness) is persisted here.
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "optionKey")
public class ProductCustomizationOption {

    @Column(name = "option_key", nullable = false, length = 60)
    private String optionKey;

    /** Admin-facing override of the catalog's default label; never blank once normalized. */
    @Column(name = "label", nullable = false, length = 120)
    private String label;

    @Column(name = "required", nullable = false)
    private boolean required;
}
