package com.ecommers.product_service.Exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {


    @Test
    void constructor_shouldSetMessageCorrectly() {

        String message = "Resource not found";

        ResourceNotFoundException exception =
                new ResourceNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException("Resource not found");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldThrowResourceNotFoundException() {

        String message = "Resource not found";

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    throw new ResourceNotFoundException(message);
                }
        );

        assertEquals(message, exception.getMessage());
    }

}