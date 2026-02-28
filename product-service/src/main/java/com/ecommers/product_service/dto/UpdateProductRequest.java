package com.ecommers.product_service.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {

    private String name;
    private String description;

    @Positive
    private BigDecimal price;

    private Integer stock;

    private String category;
}
