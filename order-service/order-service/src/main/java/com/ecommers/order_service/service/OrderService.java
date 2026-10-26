package com.ecommers.order_service.service;

import com.ecommers.order_service.dto.Request.CreateOrderRequest;
import com.ecommers.order_service.dto.Response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse createOrder(
            Long userId,
            String idempotencyKey,
            CreateOrderRequest request
    );

    OrderResponse getOrderById(Long userId, Long orderId);

    Page<OrderResponse> getMyOrders(
            Long userId,
            Pageable pageable
    );

    OrderResponse cancelOrder(Long userId, Long orderId);

}
