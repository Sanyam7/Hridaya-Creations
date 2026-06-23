package com.hridayacreations.repository;

import com.hridayacreations.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProduct_Id(Long productId, Pageable pageable);

    Optional<Review> findByUser_IdAndProduct_Id(Long userId, Long productId);

    Optional<Review> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    long countByProduct_Id(Long productId);

    @Modifying
    @Transactional
    void deleteByProduct_Id(Long productId);
}
