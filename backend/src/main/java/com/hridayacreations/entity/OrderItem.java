package com.hridayacreations.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A line item within an {@link Order}. Product name, SKU and price are snapshotted at purchase time.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_order"))
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",
            foreignKey = @ForeignKey(name = "fk_order_item_product"))
    private Product product;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "sku", length = 80)
    private String sku;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * The personalisation as it was at purchase time, each entry carrying the label and input kind
     * the customer answered under. Snapshotted like name/SKU/price, and for the same reason: the
     * order must stay readable and fulfillable after the product's configuration moves on. An admin
     * who renames "Lucky Number" to "Your Number", changes its type, or deletes it outright cannot
     * retroactively alter or erase what this customer actually asked for.
     */
    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "order_item_customization",
            joinColumns = @JoinColumn(name = "order_item_id",
                    foreignKey = @ForeignKey(name = "fk_order_item_customization_item"))
    )
    @OrderColumn(name = "display_order")
    private List<CustomizationEntry> customization = new ArrayList<>();
}
