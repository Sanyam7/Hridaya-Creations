package com.hridayacreations.entity;

import com.hridayacreations.util.CustomizationSignature;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A line item within a {@link Cart} referencing a {@link Product} and a chosen quantity.
 */
@Entity
@Table(
        name = "cart_items",
        // Identity includes the customization: two differently-personalised lines for the same
        // product are distinct rows, while re-adding an identical one merges into a quantity bump.
        uniqueConstraints = @UniqueConstraint(name = "uk_cart_item_customized",
                columnNames = {"cart_id", "product_id", "customization_signature"})
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_item_cart"))
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_item_product"))
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /**
     * The customer's personalisation, keyed by customization option. Always empty for a readymade
     * product — the server clears it rather than trusting the client.
     */
    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "cart_item_customization",
            joinColumns = @JoinColumn(name = "cart_item_id",
                    foreignKey = @ForeignKey(name = "fk_cart_item_customization_item"))
    )
    @MapKeyColumn(name = "option_key", length = 60)
    @Column(name = "option_value", length = 1000)
    private Map<String, String> customization = new LinkedHashMap<>();

    /**
     * Fixed-width digest of {@link #customization}, so the database can enforce line identity with
     * a unique constraint. Declared with a default so the column can be added to a populated table.
     */
    @Builder.Default
    @Column(name = "customization_signature", nullable = false, length = 64,
            columnDefinition = "varchar(64) default 'none'")
    private String customizationSignature = CustomizationSignature.NONE;

    /**
     * Replaces the personalisation and keeps the signature in step, so the two can never disagree.
     */
    public void applyCustomization(Map<String, String> values) {
        customization.clear();
        if (values != null) {
            customization.putAll(values);
        }
        customizationSignature = CustomizationSignature.of(customization);
    }

    /**
     * @return line total computed as {@code unitPrice * quantity}
     */
    public BigDecimal getLineTotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
