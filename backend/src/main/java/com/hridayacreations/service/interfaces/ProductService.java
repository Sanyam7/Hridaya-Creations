package com.hridayacreations.service.interfaces;

import com.hridayacreations.dto.request.CreateProductRequest;
import com.hridayacreations.dto.request.ProductSearchCriteria;
import com.hridayacreations.dto.request.UpdatePricingRequest;
import com.hridayacreations.dto.request.UpdateProductRequest;
import com.hridayacreations.dto.request.UpdateStockRequest;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

/**
 * Product catalog management (admin writes) and discovery (public reads).
 */
public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    void deleteProduct(Long id);

    ProductResponse setProductEnabled(Long id, boolean enabled);

    ProductResponse updatePricing(Long id, UpdatePricingRequest request);

    ProductResponse updateStock(Long id, UpdateStockRequest request);

    ProductResponse getProductById(Long id);

    PagedResponse<ProductResponse> searchProducts(ProductSearchCriteria criteria, Pageable pageable);
}
