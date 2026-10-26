package com.ecommers.order_service.service.impl;


import com.ecommers.order_service.client.ProductServiceClient;
import com.ecommers.order_service.dto.Request.CreateOrderItemRequest;
import com.ecommers.order_service.dto.Request.CreateOrderRequest;
import com.ecommers.order_service.dto.Response.OrderItemResponse;
import com.ecommers.order_service.dto.Response.OrderResponse;
import com.ecommers.order_service.dto.Response.ProductResponse;
import com.ecommers.order_service.entity.Order;
import com.ecommers.order_service.entity.OrderItem;
import com.ecommers.order_service.enums.OrderStatus;
import com.ecommers.order_service.exception.BadRequestException;
import com.ecommers.order_service.exception.ConflictException;
import com.ecommers.order_service.exception.ResourceNotFoundException;
import com.ecommers.order_service.repository.OrderRepository;
import com.ecommers.order_service.service.OrderService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    public OrderResponse createOrder(
            Long userId,
            String idempotencyKey,
            CreateOrderRequest request) {

        if (userId == null) {
            throw new BadRequestException(
                    "Invalid authenticated user id"
            );
        }

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new BadRequestException(
                    "A valid Idempotency-Key header is required"
            );
        }

        if (request == null
                || request.items() == null
                || request.items().isEmpty()) {
            throw new BadRequestException(
                    "Order must contain at least one item"
            );
        }

        if (orderRepository.findByUserIdAndIdempotencyKey(
                userId, idempotencyKey).isPresent()) {
            throw new ConflictException(
                    "This idempotency key has already been used"
            );
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddress(request.shippingAddress());
        order.setCurrency("INR");
        order.setOrderStatus(OrderStatus.PAYMENT_PENDING);
        order.setIdempotencyKey(idempotencyKey);

        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<Long> processedProductIds = new HashSet<>();

        // Records reservations confirmed by successful HTTP responses.
        List<CreateOrderItemRequest> reservedItems = new ArrayList<>();

        try {
            for (CreateOrderItemRequest itemRequest : request.items()) {

                if (itemRequest == null
                        || itemRequest.productId() == null
                        || itemRequest.productId() <= 0
                        || itemRequest.quantity() == null
                        || itemRequest.quantity() <= 0) {
                    throw new BadRequestException(
                            "Product ID and quantity must be positive"
                    );
                }

                Long productId = itemRequest.productId();
                int quantity = itemRequest.quantity();

                if (!processedProductIds.add(productId)) {
                    throw new BadRequestException(
                            "Duplicate product ID in order: " + productId
                    );
                }

                ProductResponse product;

                try {
                    product = productServiceClient.getProductById(productId);
                } catch (FeignException.NotFound ex) {
                    throw new ResourceNotFoundException(
                            "Product not found: " + productId
                    );
                } catch (FeignException ex) {
                    log.error(
                            "Unable to retrieve product details. productId={}, status={}",
                            productId,
                            ex.status()
                    );

                    throw new BadRequestException(
                            "Unable to retrieve product details. Please try again."
                    );
                }

                if (product == null
                        || product.id() == null
                        || !productId.equals(product.id())) {
                    throw new BadRequestException(
                            "Invalid product response for product: " + productId
                    );
                }

                if (!"ACTIVE".equalsIgnoreCase(product.status())) {
                    throw new BadRequestException(
                            "Product is not available: " + productId
                    );
                }

                if (product.price() == null
                        || product.price().signum() < 0) {
                    throw new BadRequestException(
                            "Invalid price for product: " + productId
                    );
                }

                if (product.stock() == null
                        || product.stock() < quantity) {
                    throw new BadRequestException(
                            "Insufficient stock for product: " + productId
                    );
                }

                // The stock check above is informational only.
                // The atomic reservation in Product Service is authoritative.
//                try {
//                    productServiceClient.reserveStock(productId, quantity);
//                    reservedItems.add(itemRequest);
//                } catch (FeignException ex) {
//                    log.warn(
//                            "Stock reservation failed. productId={}, status={}",
//                            productId,
//                            ex.status()
//                    );
//
//                    throw new BadRequestException(
//                            "Unable to reserve stock for product: " + productId
//                    );
//                }

                try {
                    productServiceClient.reserveStock(productId, quantity);

                    reservedItems.add(
                            new CreateOrderItemRequest(productId, quantity)
                    );

                } catch (feign.FeignException ex) {
                    log.error(
                            "Stock reservation failed. productId={}, HTTP status={}, response={}",
                            productId,
                            ex.status(),
                            ex.contentUTF8(),
                            ex
                    );

                    throw new BadRequestException(
                            "Unable to reserve stock for product: " + productId
                    );
                }

                BigDecimal unitPrice = product.price();
                BigDecimal lineTotal = unitPrice.multiply(
                        BigDecimal.valueOf(quantity)
                );

                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(product.id());
                orderItem.setProductName(product.name());
                orderItem.setUnitPrice(unitPrice);
                orderItem.setQuantity(quantity);
                orderItem.setLineTotal(lineTotal);

                order.addItem(orderItem);
                totalAmount = totalAmount.add(lineTotal);
            }

            order.setTotalAmount(totalAmount);

            Order savedOrder = orderRepository.save(order);

            return mapToResponse(savedOrder);

        } catch (RuntimeException originalException) {

            // Best-effort compensation for reservations confirmed above.
            releaseReservedStock(reservedItems);

            throw originalException;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {

        if (userId == null || userId <= 0) {
            throw new BadRequestException(
                    "Invalid authenticated user id"
            );
        }

        if (orderId == null || orderId <= 0) {
            throw new BadRequestException(
                    "Invalid order id"
            );
        }

        Order order = orderRepository
                .findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + orderId
                ));

        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(
            Long userId,
            Pageable pageable
    ) {

        if (userId == null || userId <= 0) {
            throw new BadRequestException(
                    "Invalid authenticated user id"
            );
        }

        if (pageable == null) {
            throw new BadRequestException(
                    "Pagination information is required"
            );
        }

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        if(userId == null || userId<=0){
            throw new BadRequestException(
                    "Invalid authenticated user id"
            );
        }

        if (orderId == null || orderId <= 0) {
            throw new BadRequestException(
                    "Invalid order id"
            );
        }

        Order order = orderRepository
                .findByOrderIdAndUserIdForUpdate(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + orderId
                ));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            // Already cancelled: do not release stock again.
            return mapToResponse(order);
        }

        if (order.getOrderStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new ConflictException(
                    "Only payment-pending orders can be cancelled"
            );
        }

        try {
            for (OrderItem item : order.getItems()) {
                productServiceClient.releaseStock(
                        item.getProductId(),
                        item.getQuantity()
                );
            }
        } catch (FeignException ex) {
            log.error(
                    "Stock release failed while cancelling order. "
                            + "orderId={}, status={}, response={}",
                    orderId,
                    ex.status(),
                    ex.contentUTF8(),
                    ex
            );

            throw new ConflictException(
                    "Unable to complete cancellation right now. "
                            + "Please retry or contact support."
            );
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);

        log.info(
                "Order cancelled successfully. orderId={}, userId={}",
                orderId,
                userId
        );

        return mapToResponse(savedOrder);

    }

    private void releaseReservedStock(
            List<CreateOrderItemRequest> reservedItems) {

        for (int i = reservedItems.size() - 1; i >= 0; i--) {
            CreateOrderItemRequest item = reservedItems.get(i);

            try {
                productServiceClient.releaseStock(
                        item.productId(),
                        item.quantity()
                );
            } catch (RuntimeException compensationException) {
                log.error(
                        "Stock compensation failed. productId={}, quantity={}. "
                                + "Manual reconciliation may be required.",
                        item.productId(),
                        item.quantity(),
                        compensationException
                );
            }
        }
    }

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getLineTotal()
                ))
                .toList();

        return new OrderResponse(
                order.getOrderId(),
                order.getUserId(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getShippingAddress(),
                order.getCreatedAt(),
                items
        );
    }
}
