package com.ecommers.product_service.Exception;

public class DublicateResourceException extends RuntimeException {
    public DublicateResourceException(String message) {
        super(message);
    }
}
