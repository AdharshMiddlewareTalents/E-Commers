package com.ecommers.order_service.dto.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "Shipping address is required")
        @Size(max = 1000, message = "Shipping address cannot exceed 1000 characters")
        String shippingAddress,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<CreateOrderItemRequest> items
) {
}
