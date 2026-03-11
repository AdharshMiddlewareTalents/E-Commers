package com.ecommers.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request object used to create a new product")
public class CreateProductRequest {

    @Schema(
            description = "Name of the product",
            example = "iPhone 15 Pro"
    )
    @NotBlank
    private String name;

    @Schema(
            description = "Detailed description of the product",
            example = "Latest Apple flagship smartphone"
    )
    private String description;

    @Schema(
            description = "Price of the product",
            example = "999.99"
    )
    @NotNull
    @Positive
    private BigDecimal price;

    @Schema(
            description = "Available stock quantity",
            example = "50"
    )
    @NotNull
    private Integer stock;

    @Schema(
            description = "Product category",
            example = "Mobile"
    )
    private String category;
}
