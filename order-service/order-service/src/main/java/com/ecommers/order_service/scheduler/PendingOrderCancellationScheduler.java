package com.ecommers.order_service.scheduler;

import com.ecommers.order_service.entity.Order;
import com.ecommers.order_service.enums.OrderStatus;
import com.ecommers.order_service.repository.OrderRepository;
import com.ecommers.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PendingOrderCancellationScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Value("${orders.cancellation.timeout-minutes:15}")
    private long timeoutMinutes;

    @Scheduled(fixedDelayString =
            "${orders.cancellation.scan-interval-ms:60000}")
    public void cancelExpiredOrders() {

        LocalDateTime cutoff =
                LocalDateTime.now().minusMinutes(timeoutMinutes);

        List<Order> expiredOrders =
                orderRepository.findByOrderStatusAndCreatedAtBefore(
                        OrderStatus.PAYMENT_PENDING,
                        cutoff
                );

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("Found {} expired pending orders",
                expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                orderService.cancelOrder(
                        order.getUserId(),
                        order.getOrderId()
                );

                log.info(
                        "Automatically cancelled orderId={}",
                        order.getOrderId()
                );

            } catch (Exception ex) {
                log.error(
                        "Failed to auto-cancel orderId={}. " +
                                "It may be retried during the next scan.",
                        order.getOrderId(),
                        ex
                );
            }
        }
    }

}
