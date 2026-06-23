package com.hridayacreations.repository;

import com.hridayacreations.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCart_IdAndProduct_Id(Long cartId, Long productId);

    Optional<CartItem> findByIdAndCart_Id(Long id, Long cartId);

    @Modifying
    @Transactional
    void deleteByProduct_Id(Long productId);
}
