
package com.ecommers.order_service.repository;

import com.ecommers.order_service.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderOrderId(Long orderId);
}
