package com.hridayacreations.service.impl;

import com.hridayacreations.dto.mapper.ProductMapper;
import com.hridayacreations.dto.request.CreateProductRequest;
import com.hridayacreations.dto.request.ProductSearchCriteria;
import com.hridayacreations.dto.request.UpdatePricingRequest;
import com.hridayacreations.dto.request.UpdateProductRequest;
import com.hridayacreations.dto.request.UpdateStockRequest;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.dto.response.ProductResponse;
import com.hridayacreations.entity.Category;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.enums.AuditAction;
import com.hridayacreations.entity.enums.ProductStatus;
import com.hridayacreations.exception.BusinessRuleException;
import com.hridayacreations.exception.DuplicateResourceException;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.CartItemRepository;
import com.hridayacreations.repository.CategoryRepository;
import com.hridayacreations.repository.OrderItemRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.repository.ReviewRepository;
import com.hridayacreations.repository.WishlistRepository;
import com.hridayacreations.repository.specification.ProductSpecifications;
import com.hridayacreations.service.interfaces.AuditLogService;
import com.hridayacreations.service.interfaces.ProductService;
import com.hridayacreations.util.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

/**
 * Default product catalog implementation: CRUD, availability, pricing, inventory and dynamic search.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistRepository wishlistRepository;
    private final ReviewRepository reviewRepository;
    private final ProductMapper productMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Category category = findCategory(request.getCategoryId());
        String sku = resolveSku(request.getSku(), request.getName());

        Product product = Product.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .category(category)
                .sellingPrice(request.getSellingPrice())
                .originalPrice(request.getOriginalPrice())
                .stockQuantity(request.getStockQuantity())
                .sku(sku)
                .productStatus(resolveInitialStatus(request.getProductStatus(), request.getStockQuantity()))
                .featured(Boolean.TRUE.equals(request.getFeatured()))
                .customizable(Boolean.TRUE.equals(request.getCustomizable()))
                .tags(request.getTags() != null ? new HashSet<>(request.getTags()) : new HashSet<>())
                .build();

        Product saved = productRepository.save(product);
        auditLogService.log(AuditAction.PRODUCT_CREATED, "Product", String.valueOf(saved.getId()),
                "Created product: " + saved.getName() + " (SKU " + saved.getSku() + ")");
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = findProduct(id);
        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            product.setCategory(findCategory(request.getCategoryId()));
        }
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setShortDescription(request.getShortDescription());
        product.setSellingPrice(request.getSellingPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStockQuantity(request.getStockQuantity());
        if (request.getProductStatus() != null) {
            product.setProductStatus(request.getProductStatus());
        }
        if (request.getFeatured() != null) {
            product.setFeatured(request.getFeatured());
        }
        if (request.getCustomizable() != null) {
            product.setCustomizable(request.getCustomizable());
        }
        product.getTags().clear();
        if (request.getTags() != null) {
            product.getTags().addAll(request.getTags());
        }

        Product saved = productRepository.save(product);
        auditLogService.log(AuditAction.PRODUCT_UPDATED, "Product", String.valueOf(saved.getId()),
                "Updated product: " + saved.getName());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        if (orderItemRepository.existsByProduct_Id(id)) {
            throw new BusinessRuleException(
                    "Product '%s' has existing orders and cannot be deleted. Disable it instead."
                            .formatted(product.getName()));
        }
        // Remove references that would otherwise violate foreign keys.
        cartItemRepository.deleteByProduct_Id(id);
        wishlistRepository.deleteByProduct_Id(id);
        reviewRepository.deleteByProduct_Id(id);

        productRepository.delete(product); // images are cascaded
        auditLogService.log(AuditAction.PRODUCT_DELETED, "Product", String.valueOf(id),
                "Deleted product: " + product.getName());
    }

    @Override
    @Transactional
    public ProductResponse setProductEnabled(Long id, boolean enabled) {
        Product product = findProduct(id);
        product.setProductStatus(enabled
                ? (product.isInStock() ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK)
                : ProductStatus.INACTIVE);
        Product saved = productRepository.save(product);
        auditLogService.log(AuditAction.PRODUCT_UPDATED, "Product", String.valueOf(id),
                (enabled ? "Enabled" : "Disabled") + " product: " + saved.getName());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updatePricing(Long id, UpdatePricingRequest request) {
        Product product = findProduct(id);
        product.setSellingPrice(request.getSellingPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        Product saved = productRepository.save(product);
        auditLogService.log(AuditAction.PRICING_UPDATED, "Product", String.valueOf(id),
                "Updated pricing for: " + saved.getName() + " -> selling " + saved.getSellingPrice());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateStock(Long id, UpdateStockRequest request) {
        Product product = findProduct(id);
        product.setStockQuantity(request.getStockQuantity());
        // Keep availability consistent with stock unless the product was explicitly retired.
        if (product.getProductStatus() != ProductStatus.INACTIVE
                && product.getProductStatus() != ProductStatus.DISCONTINUED) {
            product.setProductStatus(product.isInStock() ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK);
        }
        Product saved = productRepository.save(product);
        auditLogService.log(AuditAction.INVENTORY_UPDATED, "Product", String.valueOf(id),
                "Updated stock for: " + saved.getName() + " -> " + saved.getStockQuantity());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return productMapper.toResponse(findProduct(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        Specification<Product> specification = ProductSpecifications.build(
                criteria.getKeyword(),
                criteria.getCategoryId(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getTag(),
                criteria.getFeatured(),
                criteria.getStatus());
        Page<Product> page = productRepository.findAll(specification, pageable);
        return PagedResponse.from(page, productMapper::toResponse);
    }

    /* ----------------------------------------------------------------- */

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }

    private String resolveSku(String requestedSku, String productName) {
        if (requestedSku != null && !requestedSku.isBlank()) {
            String sku = requestedSku.trim();
            if (productRepository.existsBySkuIgnoreCase(sku)) {
                throw new DuplicateResourceException("Product", "SKU", sku);
            }
            return sku;
        }
        String generated;
        do {
            generated = ReferenceGenerator.generateSku(productName);
        } while (productRepository.existsBySkuIgnoreCase(generated));
        return generated;
    }

    private ProductStatus resolveInitialStatus(ProductStatus requested, Integer stock) {
        if (requested != null) {
            return requested;
        }
        return (stock != null && stock > 0) ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK;
    }
}
