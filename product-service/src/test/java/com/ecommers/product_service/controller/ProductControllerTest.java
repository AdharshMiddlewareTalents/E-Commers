package com.ecommers.product_service.controller;

import com.ecommers.product_service.dto.CreateProductRequest;
import com.ecommers.product_service.dto.ProductResponse;
import com.ecommers.product_service.dto.UpdateProductRequest;
import com.ecommers.product_service.entity.ProductStatus;
import com.ecommers.product_service.security.JwtUtil;
import com.ecommers.product_service.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProductService service;

    @MockBean
    JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void createProduct_shouldReturnCreated() throws Exception {

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Laptop");
        request.setDescription("Gaming Laptop");
        request.setPrice(new BigDecimal("1200"));
        request.setStock(10);
        request.setCategory("Electronics");

        ProductResponse response =ProductResponse.builder()
                        .id(1L)
                        .name("Laptop")
                        .slug("laptop")
                        .price(new BigDecimal(1200))
                        .stock(10)
                        .status(ProductStatus.ACTIVE)
                        .build();

        when(service.createProduct(any(),any())).thenReturn(response);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username ="admin@test.com", roles = "ADMIN")
    void updateProduct_shouldReturnOk() throws Exception {

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Updated Laptop");
        request.setDescription("Updated description");
        request.setPrice(new BigDecimal("2000"));
        request.setStock(15);
        request.setCategory("Updating category");

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Updated Laptop")
                .slug("updated-laptop")
                .description("Updated description")
                .price(new BigDecimal("2000"))
                .stock(15)
                .category("Updating category")
                .status(ProductStatus.ACTIVE)
                .build();

        when(service.update(anyLong(),any(UpdateProductRequest.class),anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/products/{id}",1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"))
                .andExpect(jsonPath("$.price").value(2000))
                .andExpect(jsonPath("$.stock").value(15));

    }

    @Test
    void getAllProduct_shouldReturnPage() throws Exception {

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Laptop")
                .slug("laptop")
                .price(new BigDecimal("1200"))
                .stock(10)
                .category("Electronics")
                .status(ProductStatus.ACTIVE)
                .build();

        Page<ProductResponse> page =
                new PageImpl<>(List.of(response));

        when(service.getALl(any(),any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/products")
                .param("keyword","Laptop")
                .param("page","0")
                .param("size","10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"))
                .andExpect(jsonPath("$.content[0].stock").value(10))
                .andExpect(jsonPath("$.content[0].category").value("Electronics"));
    }

    @Test
    void getProductById_shouldReturnProduct() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Laptop")
                .slug("laptop")
                .description("Gaming Laptop")
                .price(new BigDecimal("1200"))
                .stock(10)
                .category("Electronics")
                .status(ProductStatus.ACTIVE)
                .build();

        when(service.getProudutById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/products/{id}",1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value("1200"))
                .andExpect(jsonPath("$.stock").value("10"));
    }

    @Test
    void getProductBySlug_shouldReturnProduct() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Laptop")
                .slug("laptop")
                .description("Gaming Laptop")
                .price(new BigDecimal("1200"))
                .stock(10)
                .category("Electronics")
                .status(ProductStatus.ACTIVE)
                .build();

        when(service.getBySlug("laptop"))
                .thenReturn(response);

        mockMvc.perform(get("/products/slug/{slug}","laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("laptop"))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value("1200"));



    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void changeProductStatus_shoudldChangeStatus() throws Exception {


       doNothing().when(service)
               .changeStatus(anyLong(),any(ProductStatus.class),anyString());

       mockMvc.perform(
               patch("/products/{id}/status",1)
                       .param("status","ACTIVE")
       )
               .andExpect(status().isOk())
               .andExpect(content().string("Product status updated"));

       verify(service).changeStatus(1L,ProductStatus.ACTIVE,"admin@test.com");


    }

    @Test
    @WithMockUser(username = "admin@test.com",roles = "ADMIN")
    void deleteProduct_shouldDeleteTheProduct() throws Exception {
        doNothing().when(service)
                .delete(anyLong(),anyString());

        mockMvc.perform(delete("/products/{id}",1))
                .andExpect(status().isOk())
                .andExpect(content().string("Product archived successfully"));

        verify(service).delete(1L,"admin@test.com");
    }

    @Test
    @WithMockUser(username = "admin@test.com",roles = "ADMIN")
    void reduceStock_shouldReturnSuccess() throws Exception {
        mockMvc.perform(
                patch("/products/{id}/reduce-stock",1L)
                        .param("quantity",String.valueOf(10))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().string("Stock reduced successfully"));

        verify(service).reduceStock(1L,10);
    }
}