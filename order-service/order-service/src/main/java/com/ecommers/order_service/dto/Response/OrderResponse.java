package com.ecommers.order_service.dto.Response;

import com.ecommers.order_service.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long userId,
        OrderStatus orderStatus,
        BigDecimal totalAmount,
        String currency,
        String shippingAddress,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}
