package com.hridayacreations.repository;

import com.hridayacreations.entity.Order;
import com.hridayacreations.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByIdAndUser_Id(Long id, Long userId);

    Optional<Order> findByOrderNumberAndUser_Id(String orderNumber, Long userId);

    Page<Order> findByUser_Id(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    boolean existsByOrderNumber(String orderNumber);

    /**
     * Determines whether a user has a non-cancelled order containing the given product,
     * which qualifies them to review that product.
     */
    @Query("""
            SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END
            FROM OrderItem oi
            WHERE oi.product.id = :productId
              AND oi.order.user.id = :userId
              AND oi.order.status <> com.hridayacreations.entity.enums.OrderStatus.CANCELLED
            """)
    boolean hasUserPurchasedProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}
