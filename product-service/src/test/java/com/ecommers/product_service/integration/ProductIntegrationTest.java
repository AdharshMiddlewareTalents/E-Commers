package com.ecommers.product_service.integration;

import com.ecommers.product_service.dto.CreateProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private DataSource dataSource;

    @Test
    void printDataSource() throws Exception {
        Connection connection = dataSource.getConnection();
        System.out.println("DB URL = " + connection.getMetaData().getURL());
    }


    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void testCreateProduct() throws Exception{

        CreateProductRequest request = new CreateProductRequest();
        request.setName("iPhone 17");
        request.setDescription("Smart phone");
        request.setCategory("Electronics");
        request.setPrice(new BigDecimal("1000"));
        request.setStock(10);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("iPhone 17"));

    }

    @Test
    @WithMockUser(username = "admin@test.com",roles = {"ADMIN"})
    void testCreateProduct_dublicate() throws Exception{
        CreateProductRequest request = new CreateProductRequest();
        request.setName("iPhone 19");
        request.setPrice(new BigDecimal("1000"));
        request.setStock(10);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void testCreateProduct_validationFail() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("");

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testCreateProduct_unauthorized() throws Exception {

        CreateProductRequest request = new CreateProductRequest();
        request.setName("iPhone 17");

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

}
