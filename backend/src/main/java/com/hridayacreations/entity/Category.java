package com.hridayacreations.entity;

import com.hridayacreations.entity.enums.CategoryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Catalog category that groups related products (e.g. Personalized Mugs, Photo Frames).
 */
@Entity
@Table(
        name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_category_name", columnNames = "category_name")
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Category extends BaseEntity {

    @Column(name = "category_name", nullable = false, length = 120)
    private String categoryName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CategoryStatus status = CategoryStatus.ACTIVE;
}
