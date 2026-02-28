package com.ecommers.product_service.service;

import com.ecommers.product_service.Exception.BadRequestException;
import com.ecommers.product_service.Exception.DublicateResourceException;
import com.ecommers.product_service.Exception.ResourceNotFoundException;
import com.ecommers.product_service.dto.CreateProductRequest;
import com.ecommers.product_service.dto.ProductResponse;
import com.ecommers.product_service.dto.UpdateProductRequest;
import com.ecommers.product_service.entity.Product;
import com.ecommers.product_service.entity.ProductStatus;
import com.ecommers.product_service.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductService productService;


    private Product product;

    @BeforeEach
    void setUp(){
        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .slug("laptop")
                .description("gaming laptop")
                .price(new BigDecimal(50000))
                .stock(10)
                .status(ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }


    @Test
    void createProduct_ShouldReturnProductResponse() throws IOException {

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Laptop");
        request.setDescription("Gaming Laptop");
        request.setPrice(new BigDecimal("50000"));
        request.setStock(10);
        request.setCategory("Electronics");

        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.findBySlugAndActiveTrue(anyString())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request, "admin@test.com");

        assertNotNull(response);
        assertEquals("Laptop", response.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_ShouldThrowException_WhenSlugAlreadyExists(){

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Laptop");
        request.setDescription("Gaming laptop");
        request.setPrice(new BigDecimal("50000"));
        request.setStock(10);
        request.setCategory("Electronics");

        Product existingProduct = Product.builder()
                        .id(1L)
                        .name("Laptop")
                        .slug("laptop")
                        .build();

        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.findBySlugAndActiveTrue(anyString())).thenReturn(Optional.of(product));
        assertThrows(DublicateResourceException.class,()->
                productService.createProduct(request,"admin@test.com"));

        verify(productRepository,never()).save(any(Product.class));
    }


    @Test
    void getProductById_ShouldReturnProduct(){
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        ProductResponse response = productService.getProudutById(1L);
        assertEquals("Laptop", response.getName());
    }

    @Test
    void update_ShouldUpdateProduct(){
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Update Laptop");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response =
                productService.update(1L,request,"admin@test.com");

        assertEquals("Update Laptop",response.getName());
    }

//    void update_ShouldThrowException_WhenProductNotFound(){
//        when(productRepository.findById(1L)).thenReturn(Optional.empty());
//
//        UpdateProductRequest request = new UpdateProductRequest();
//
//        assertThrows(ResourceNotFoundException.class,
//                ()->productService.update())
//
//    }


    @Test
    void delete_ShouldArchievedProduct(){
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L,"admintest.com");
        assertFalse(product.isActive());
        assertEquals(ProductStatus.ARCHIVED,product.getStatus());
        verify(productRepository).save(product);
    }


    @Test
    void reduceStock_ShouldReduceStock(){
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        productService.reduceStock(1L,5);
        assertEquals(5,product.getStock());
    }


    @Test
    void reduceStock_shouldThrowException_WhenInsufficient(){
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        assertThrows(BadRequestException.class,
                ()-> productService.reduceStock(1L,50));
    }


    @Test
    void getAll_ShouldReturnPage(){
        Pageable pageable = PageRequest.of(0,10);
        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findByActiveTrue(any(Pageable.class)))
                .thenReturn(page);

        Page<ProductResponse> responses=
                productService.getALl(null,pageable);

        assertEquals(1,responses.getTotalElements());
    }


    @Test
    void getBySlug_ShouldReturnProduct(){

        when(productRepository.findBySlugAndActiveTrue("laptop"))
                .thenReturn(Optional.of(product));

        ProductResponse response = productService.getBySlug("laptop");

        assertEquals("Laptop",response.getName());

    }
}