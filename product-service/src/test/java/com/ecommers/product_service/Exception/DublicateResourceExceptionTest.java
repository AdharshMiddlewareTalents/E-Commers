package com.ecommers.product_service.Exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DublicateResourceExceptionTest {

    @Test
    void constructor_shouldSetMessageCorrectly() {

        String message = "Resource already exists";

        DublicateResourceException exception =
                new DublicateResourceException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {

        DublicateResourceException exception =
                new DublicateResourceException("Duplicate resource");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldThrowDublicateResourceException() {

        String message = "Duplicate resource";

        DublicateResourceException exception = assertThrows(
                DublicateResourceException.class,
                () -> {
                    throw new DublicateResourceException(message);
                }
        );

        assertEquals(message, exception.getMessage());
    }

}