package com.ecommers.order_service.controller;

import com.ecommers.order_service.dto.Request.CreateOrderRequest;
import com.ecommers.order_service.dto.Response.OrderResponse;
import com.ecommers.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateOrderRequest request
    ){

        OrderResponse response = orderService.createOrder(
                userId,
                idempotencyKey,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }


    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                orderService.getOrderById(userId, orderId)
        );
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                orderService.getMyOrders(userId, pageable)
        );
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId
    ) {
        OrderResponse response =
                orderService.cancelOrder(userId, orderId);

        return ResponseEntity.ok(response);
    }

}
