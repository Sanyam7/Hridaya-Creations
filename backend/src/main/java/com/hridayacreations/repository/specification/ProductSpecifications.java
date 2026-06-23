package com.hridayacreations.repository.specification;

import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.enums.ProductStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds dynamic, type-safe {@link Specification}s for product search supporting filtering by
 * keyword, category, price range, tag, featured flag and status.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Specification<Product> build(String keyword,
                                               Long categoryId,
                                               BigDecimal minPrice,
                                               BigDecimal maxPrice,
                                               String tag,
                                               Boolean featured,
                                               ProductStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("shortDescription")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            }

            if (featured != null) {
                predicates.add(cb.equal(root.get("featured"), featured));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("productStatus"), status));
            }

            if (StringUtils.hasText(tag)) {
                Join<Object, Object> tagsJoin = root.join("tags");
                predicates.add(cb.equal(cb.lower(tagsJoin.as(String.class)), tag.toLowerCase()));
                if (query != null) {
                    query.distinct(true);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
