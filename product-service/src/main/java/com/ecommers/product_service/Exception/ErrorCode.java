package com.ecommers.product_service.Exception;

public enum ErrorCode {
    PRODUCT_NOT_FOUND("Product not found with id: %d");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
