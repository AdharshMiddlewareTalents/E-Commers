package com.ecommers.product_service.Exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Standard error response returned when an API request fails")
public class ErrorResponse {
    @Schema(
            description = "Time when the error occurred",
            example = "2026-03-06T12:30:45"
    )
    private LocalDateTime timestamp;


    @Schema(
            description = "HTTP status code",
            example = "404"
    )
    private int status;

    @Schema(
            description = "HTTP error type",
            example = "Not Found"
    )
    private String error;

    @Schema(
            description = "Detailed error message",
            example = "Product not found with id: 10"
    )
    private String message;

    @Schema(
            description = "API endpoint path that caused the error",
            example = "/api/products/10"
    )
    private String path;
}
