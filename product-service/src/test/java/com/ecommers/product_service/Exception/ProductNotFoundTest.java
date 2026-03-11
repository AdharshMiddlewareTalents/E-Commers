package com.ecommers.product_service.Exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductNotFoundTest {


    @Test
    void constructor_shouldSetMessageCorrectly() {

        String message = "Product not found";

        ProductNotFound exception = new ProductNotFound(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {

        ProductNotFound exception = new ProductNotFound("Product not found");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldThrowProductNotFoundException() {

        String message = "Product not found";

        ProductNotFound exception = assertThrows(
                ProductNotFound.class,
                () -> {
                    throw new ProductNotFound(message);
                }
        );

        assertEquals(message, exception.getMessage());
    }

}