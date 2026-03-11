package com.ecommers.product_service.Exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp(){
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/products/1");
    }

    @Test
    void handleResourceNotFound_shouldReturn404(){
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Product not found");

        ResponseEntity<ErrorResponse> response =
                handler.handleProductNotFound(ex,request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Product not found",response.getBody().getMessage());
        assertEquals("/products/1",response.getBody().getPath());
        assertEquals(404,response.getBody().getStatus());
    }

    @Test
    void handleProductNotFound() {
    }

    @Test
    void handleBadRequest_shouldReturn400() {
        BadRequestException ex =
                new BadRequestException("Invalid request");

        ResponseEntity<ErrorResponse> response =
                handler.handleBadRequest(ex,request);

        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
        assertEquals("Invalid request",response.getBody().getMessage());
        assertEquals(400,response.getBody().getStatus());
    }


    @Test
    void handleAccessDenied_shouldReturn403() {

        org.springframework.security.access.AccessDeniedException ex =
                new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().getMessage());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void handleDublicate_shouldReturn409() {
        DublicateResourceException ex =
                new DublicateResourceException("Product already exists");

        ResponseEntity<ErrorResponse> response =
                handler.handleDublicate(ex,request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Product already exists", response.getBody().getMessage());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void handleGenericException_shouldReturn500() {

        Exception ex = new Exception("Internal error");

        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal error", response.getBody().getMessage());
        assertEquals(500, response.getBody().getStatus());
    }
}