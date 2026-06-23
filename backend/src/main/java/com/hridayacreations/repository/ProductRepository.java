package com.hridayacreations.repository;

import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCase(String sku);

    Page<Product> findByFeaturedTrueAndProductStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategory_IdAndProductStatus(Long categoryId, ProductStatus status, Pageable pageable);

    long countByCategory_Id(Long categoryId);
}
