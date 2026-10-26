package com.ecommers.product_service.Exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BadRequestExceptionTest {

    @Test
    void constructor_shouldSetMessageCorrectly(){
        String message = "Invalid request";
        BadRequestException exception = new BadRequestException(message);
        assertEquals(message,exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {

        BadRequestException exception =
                new BadRequestException("Invalid request");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldThrowBadRequestException() {

        String message = "Invalid request";

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> {
                    throw new BadRequestException(message);
                }
        );

        assertEquals(message, exception.getMessage());
    }
}