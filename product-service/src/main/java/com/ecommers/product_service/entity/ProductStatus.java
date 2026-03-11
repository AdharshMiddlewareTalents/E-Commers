package com.ecommers.product_service.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of the product")
public enum ProductStatus {

    @Schema(description = "Product is available for purchase")
    ACTIVE,

    @Schema(description = "Product is temporarily disabled")
    INACTIVE,

    @Schema(description = "Product is out of stock")
    OUT_OF_STOCK,

    @Schema(description = "Product is removed")
    ARCHIVED
}
