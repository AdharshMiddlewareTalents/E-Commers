package com.ecommers.product_service.Exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessDeniedExceptionTest {

    @Test
    void constructor_shouldSetMessageCorrectly(){
        String message = "Access denied";
        AccessDeniedException exception = new AccessDeniedException(message);
        assertEquals(message,exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfRuntimeException(){
        AccessDeniedException exception =
                new AccessDeniedException("Access denied");

        assertTrue(exception instanceof RuntimeException);
    }

}