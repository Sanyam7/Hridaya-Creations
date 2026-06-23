package com.hridayacreations.entity;

import com.hridayacreations.entity.enums.ProductStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A sellable, optionally customizable gifting product belonging to a {@link Category}.
 */
@Entity
@Table(
        name = "products",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_sku", columnNames = "sku"),
        indexes = {
                @Index(name = "idx_product_name", columnList = "name"),
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_status", columnList = "product_status")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_category"))
    private Category category;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "original_price", precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Builder.Default
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "sku", nullable = false, length = 80)
    private String sku;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false, length = 20)
    private ProductStatus productStatus = ProductStatus.ACTIVE;

    @Builder.Default
    @Column(name = "featured", nullable = false)
    private boolean featured = false;

    @Builder.Default
    @Column(name = "customizable", nullable = false)
    private boolean customizable = false;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "product_tags",
            joinColumns = @JoinColumn(name = "product_id",
                    foreignKey = @ForeignKey(name = "fk_product_tags_product"))
    )
    @Column(name = "tag", length = 60)
    private Set<String> tags = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, id ASC")
    private List<ProductImage> images = new ArrayList<>();

    @Builder.Default
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount = 0;

    /* ----- Convenience methods ----- */

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }
}
