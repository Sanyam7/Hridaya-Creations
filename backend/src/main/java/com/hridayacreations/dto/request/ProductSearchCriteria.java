package com.hridayacreations.dto.request;

import com.hridayacreations.entity.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Filter holder for product search. Not a request body — assembled from query parameters by the
 * controller layer.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {

    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String tag;
    private Boolean featured;
    private ProductStatus status;
}
