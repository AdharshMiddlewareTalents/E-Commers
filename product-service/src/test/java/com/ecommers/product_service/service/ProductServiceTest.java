package com.ecommers.product_service.service;

import com.ecommers.product_service.dto.CreateProductRequest;
import com.ecommers.product_service.entity.Product;
import com.ecommers.product_service.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductService productService;

    @Test
    void successfullyAddProduct() throws IOException {
        CreateProductRequest request = new CreateProductRequest();
        String email = "";
        productService.createProduct(request,email);
    }

    @Test
    void update() {
    }

    @Test
    void getALl() {
    }

    @Test
    void getProudutById() {
    }

    @Test
    void getBySlug() {
    }

    @Test
    void changeStatus() {
    }

    @Test
    void delete() {
    }

    @Test
    void reduceStock() {
    }
}