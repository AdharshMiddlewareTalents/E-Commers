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
import java.lang.reflect.Method;
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
                .category("Electronics")
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
    void getProductById_shouldThrowResorceNotFoundExcetption_ifNotFound(){

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class,
                        ()-> productService.getProudutById(1L));

        assertEquals("Product not found",exception.getMessage());

        verify(productRepository).findById(1L);

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

    @Test
    void update_ShouldThrowException_WhenProductNotFound(){
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        UpdateProductRequest request = new UpdateProductRequest();

        assertThrows(ResourceNotFoundException.class,
                ()->productService.update(1L,request,"admin@test.com"));
        verify(productRepository,never()).save(any());

    }


    @Test
    void update_shouldNotChangeSlug_WhenNameIsNull(){
        UpdateProductRequest request = new UpdateProductRequest();
        request.setDescription("Only Description Updated");
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        productService.update(1L,request,"admin@test.com");
        assertEquals("laptop",product.getSlug());
    }


    @Test
    void update_shouldNotChangeDescription_WhenDescriptionIsNull(){
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("new name");
        when((productRepository.findById(1L)))
                .thenReturn(Optional.of(product));
        productService.update(1L,request,"admin@test.com");
        assertEquals("gaming laptop",product.getDescription());
    }


    @Test
    void update_shouldNotChangePrice_WhenPriceIsNull(){
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("new name");
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        productService.update(1L,request,"admin@test.com");
        assertEquals(BigDecimal.valueOf(50000),product.getPrice());
    }


    @Test
    void update_shouldNotChangeCategory_whenCategoryIsNull() {

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("New Name");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        productService.update(1L, request, "admin@test.com");

        assertEquals("Electronics", product.getCategory());
    }




    @Test
    void delete_ShouldArchievedProduct(){
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L,"admintest.com");
        assertFalse(product.isActive());
        assertEquals(ProductStatus.ARCHIVED,product.getStatus());
        verify(productRepository).save(product);
    }

    @Test
    void delete_shouldThrowException_ifProductNotFound(){
        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());
        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class,
                        ()-> productService.delete(1L,"admin@test.com"));

        assertEquals("Product not found",exception.getMessage());

        verify(productRepository).findById(1L);
        verify(productRepository,never()).save(any());
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
        BadRequestException exception =
        assertThrows(BadRequestException.class,
                ()-> productService.reduceStock(1L,50));
        assertEquals("insufficient stock",exception.getMessage());

        verify(productRepository).findById(1L);
        verify(productRepository,never()).save(any());
    }

    @Test
    void reduceStock_shouldThrowException_whenProductNotFound(){
        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());
        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class,
                        ()->productService.reduceStock(1L,50));

        assertEquals("product not found",exception.getMessage());

        verify(productRepository).findById(1L);
        verify(productRepository,never()).save(any());
    }


    @Test
    void reduceStock_shouldSetOutOfStock_whenStocksBecomesZero(){

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        productService.reduceStock(1L,10);
        assertEquals(0,product.getStock());
        assertEquals(ProductStatus.OUT_OF_STOCK,product.getStatus());

        verify(productRepository).save(product);

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
    void getAll_shouldSearchByKeyword_whenKeywordIsPresent(){

        Pageable pageable = PageRequest.of(0,10);
        Page<Product> productPage =
                new PageImpl<>(List.of(product));

        when(productRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(
                        eq("laptop"),any(Pageable.class)
                )).thenReturn(productPage);

        Page<ProductResponse> responses =
                productService.getALl("laptop",pageable);

        assertEquals(1L,responses.getTotalElements());

        verify(productRepository)
                .findByNameContainingIgnoreCaseAndActiveTrue(
                        eq("laptop"),any(Pageable.class)
                );

        verify(productRepository,never())
                .findByActiveTrue(any());

    }


    @Test
    void getAll_shouldCallFindByActiveTrue_whenKeywordIsNull() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productPage =
                new PageImpl<>(List.of(new Product()));

        when(productRepository.findByActiveTrue(any(Pageable.class)))
                .thenReturn(productPage);

        Page<ProductResponse> response =
                productService.getALl(null, pageable);

        assertEquals(1, response.getTotalElements());

        verify(productRepository)
                .findByActiveTrue(any(Pageable.class));

        verify(productRepository, never())
                .findByNameContainingIgnoreCaseAndActiveTrue(
                        anyString(), any());
    }

    @Test
    void getAll_shouldCallFindByActiveTrue_whenKeywordIsEmpty() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productPage =
                new PageImpl<>(List.of(new Product()));

        when(productRepository.findByActiveTrue(any(Pageable.class)))
                .thenReturn(productPage);

        Page<ProductResponse> response =
                productService.getALl("", pageable);

        assertEquals(1, response.getTotalElements());

        verify(productRepository)
                .findByActiveTrue(any(Pageable.class));

        verify(productRepository, never())
                .findByNameContainingIgnoreCaseAndActiveTrue(
                        anyString(), any());
    }

    @Test
    void getAll_shouldLimitPageSizeTo50() {

        Pageable pageable = PageRequest.of(0, 100); // more than 50

        when(productRepository.findByActiveTrue(any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable passedPageable = invocation.getArgument(0);
                    assertEquals(50, passedPageable.getPageSize());
                    return new PageImpl<>(List.of());
                });

        productService.getALl(null, pageable);
    }


    @Test
    void getBySlug_ShouldReturnProduct(){

        when(productRepository.findBySlugAndActiveTrue("laptop"))
                .thenReturn(Optional.of(product));

        ProductResponse response = productService.getBySlug("laptop");

        assertEquals("Laptop",response.getName());

    }


    @Test
    void getBySlug_shouldThrow_ResorceNotFound(){
        when((productRepository.findBySlugAndActiveTrue("laptop")))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class,
                        ()-> productService.getBySlug("laptop"));

        assertEquals("Product not found",exception.getMessage());

        verify(productRepository).findBySlugAndActiveTrue("laptop");

    }

//    @Test
//    void generateUniqueSlug_shouldReturnBaseSlug_whenSlugDoesNotExist() throws Exception {
//
//        when(productRepository.existsBySlug("laptop"))
//                .thenReturn(false);
//
//        Method method = ProductService.class
//                .getDeclaredMethod("generateUniqueSlug", String.class);
//        String result = (String) method.invoke(productService,"laptop");
//        method.setAccessible(true);
//        assertEquals("laptop",result);
//        verify(productRepository).existsBySlug("laptop");
//    }

    @Test
    void changeStatus_shouldUpgradeProductSuccessfully(){
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.changeStatus(1L,ProductStatus.ACTIVE,"admin@test.com");
        assertEquals(ProductStatus.ACTIVE,product.getStatus());
        assertEquals("admin@test.com",product.getUpdatedBy());
        assertNotNull(product.getUpdatedAt());

        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }


    @Test
    void changeStatus_shouldThrowException_whenProductNotFound(){
        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());
        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class,
                        ()-> productService.changeStatus(
                                1L,
                                ProductStatus.ACTIVE,
                                "admin@test.com"
                        ));
        assertEquals("Product not found",exception.getMessage());
        verify(productRepository).findById(1L);
        verify(productRepository,never()).save(any());
    }
}