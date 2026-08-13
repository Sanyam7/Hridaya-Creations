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
 * One colour a {@link Product} is available in, stored as an element collection row.
 *
 * <p>{@code colorId} is the stable normalized key (e.g. {@code "red"}) and carries the identity of
 * the value — equality deliberately ignores the display fields so a product can never hold the same
 * colour twice, even if its name or hex code were edited.
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "colorId")
public class ProductColor {

    @Column(name = "color_id", nullable = false, length = 40)
    private String colorId;

    @Column(name = "color_name", nullable = false, length = 60)
    private String name;

    @Column(name = "hex_code", nullable = false, length = 7)
    private String hexCode;
}
