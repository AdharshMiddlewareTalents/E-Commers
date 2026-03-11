package com.ecommers.product_service.dto;

import com.ecommers.product_service.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response object representing product details")
public class ProductResponse {

    @Schema(
            description = "Unique product ID",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Product name",
            example = "iPhone 15 Pro"
    )
    private String name;

    @Schema(
            description = "SEO friendly slug",
            example = "iphone-15-pro"
    )
    private String slug;

    @Schema(
            description = "Product description",
            example = "Latest Apple flagship smartphone"
    )
    private String description;

    @Schema(
            description = "Product price",
            example = "999.99"
    )
    private BigDecimal price;

    @Schema(
            description = "Available stock",
            example = "50"
    )
    private Integer stock;

    @Schema(
            description = "Product category",
            example = "MOBILE"
    )
    private String category;

    @Schema(
            description = "URL of the product image",
            example = "https://cdn.store.com/products/iphone15.jpg"
    )
    private String imageUrl;

    @Schema(
            description = "Current status of the product",
            example = "ACTIVE"
    )
    private ProductStatus status;
}
