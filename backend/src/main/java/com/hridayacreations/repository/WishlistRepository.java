package com.hridayacreations.repository;

import com.hridayacreations.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Page<Wishlist> findByUser_Id(Long userId, Pageable pageable);

    Optional<Wishlist> findByUser_IdAndProduct_Id(Long userId, Long productId);

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    @Modifying
    @Transactional
    void deleteByUser_IdAndProduct_Id(Long userId, Long productId);

    @Modifying
    @Transactional
    void deleteByProduct_Id(Long productId);
}
