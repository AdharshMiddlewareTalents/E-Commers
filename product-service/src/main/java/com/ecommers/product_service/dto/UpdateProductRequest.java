package com.ecommers.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request object used to update product details")
public class UpdateProductRequest {

    @Schema(
            description = "Updated product name",
            example = "iPhone 15 Pro Max"
    )
    private String name;

    @Schema(
            description = "Updated product description",
            example = "Updated Apple flagship smartphone"
    )
    private String description;

    @Schema(
            description = "Updated price",
            example = "1099.99"
    )
    @Positive
    private BigDecimal price;

    @Schema(
            description = "Updated stock quantity",
            example = "40"
    )
    private Integer stock;

    @Schema(
            description = "Updated product category",
            example = "MOBILE"
    )
    private String category;
}
