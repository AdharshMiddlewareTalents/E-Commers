
package com.ecommers.order_service.repository;

import com.ecommers.order_service.entity.Order;
import com.ecommers.order_service.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderIdAndUserId(Long orderId, Long userId);

    Page<Order> findByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    Optional<Order> findByUserIdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT o
       FROM Order o
       WHERE o.orderId = :orderId
         AND o.userId = :userId
       """)
    Optional<Order> findByOrderIdAndUserIdForUpdate(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );

    List<Order> findByOrderStatusAndCreatedAtBefore(
            OrderStatus orderStatus,
            LocalDateTime cutoff
    );




}
