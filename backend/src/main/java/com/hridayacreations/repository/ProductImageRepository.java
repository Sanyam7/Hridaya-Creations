package com.hridayacreations.repository;

import com.hridayacreations.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProduct_IdOrderByDisplayOrderAsc(Long productId);

    Optional<ProductImage> findByIdAndProduct_Id(Long id, Long productId);

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.primaryImage = false WHERE pi.product.id = :productId")
    void clearPrimaryFlagForProduct(@Param("productId") Long productId);
}
