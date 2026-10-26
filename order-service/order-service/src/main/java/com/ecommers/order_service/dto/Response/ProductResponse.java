package com.ecommers.order_service.dto.Response;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        Integer stock,
        String category,
        String imageUrl,
        String status
) {
}
