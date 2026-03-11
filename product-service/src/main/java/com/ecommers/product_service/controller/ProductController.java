package com.ecommers.product_service.controller;

import com.ecommers.product_service.dto.CreateProductRequest;
import com.ecommers.product_service.dto.ProductResponse;
import com.ecommers.product_service.dto.UpdateProductRequest;
import com.ecommers.product_service.entity.ProductStatus;
import com.ecommers.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;



@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product API",description = "Operation related to products")
public class ProductController {

    private final ProductService service;

    @Operation(
            summary = "Create new product",
            description ="Admin can add new product for there store" )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",description = "Product created successfully"),
            @ApiResponse(responseCode = "400",description = "Invalid product request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403",description = "Access denied - Only ADMIN can create products"),
            @ApiResponse(responseCode = "500",description = "Internal server error"),
            @ApiResponse(responseCode = "409", description = "Product already exists"),
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "product details required to create a new product",
                    required = true
            )
            @RequestBody @Valid CreateProductRequest request

    ) throws IOException {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        ProductResponse response = service.createProduct(request,email);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }


    @Operation(
            summary = "Update product",
            description = "Admin can update product details such as name,price,description and category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Access denied - Only ADMIN can update products"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @Parameter(description = "Product Id", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated product information",
                    required = true
            )
            @RequestBody @Valid UpdateProductRequest request

    ){
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();


        return ResponseEntity.ok(
                service.update(id,request,email)
        );
    }

    @Operation(
            summary = "Get all products",
            description = "Retrieve all products with optional keyword search and pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(
            @Parameter(
                    description = "Keyword for searching products by name or category",
                    example = "iphone"
            )
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Pagination information")
            Pageable pageable
    ){
        Page<ProductResponse> responses=
                service.getALl(keyword, pageable);

       return ResponseEntity.ok(responses);
    }


    @Operation(
            summary = "Get product by ID",
            description = "Retrieve a single product using its unique ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.getProudutById(id)
        );
    }


    @Operation(
            summary = "Get product by slug",
            description = "Retrieve a product using its SEO-friendly slug"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getBySlug(
            @Parameter(description = "Product slug", example = "iphone-15-pro")
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(
                service.getBySlug(slug)
        );
    }


    @Operation(
            summary = "Change product status",
            description = "Admin can change product status (ACTIVE, INACTIVE, OUT_OF_STOCK)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - Only ADMIN can change product status"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> changeStatus(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id,

            @Parameter(description = "New product status", example = "ACTIVE")
            @RequestParam ProductStatus status

    ) {

        String email = SecurityContextHolder
                .getContext()
                        .getAuthentication()
                                .getName();

        service.changeStatus(id, status, email);

        return ResponseEntity.ok("Product status updated");
    }


    @Operation(
            summary = "Delete product",
            description = "Admin can archive a product instead of permanently deleting it"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product archived successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - Only ADMIN can delete products"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id
    ) {

        String email = SecurityContextHolder
                .getContext()
                        .getAuthentication()
                                .getName();

        service.delete(id, email);

        return ResponseEntity.ok("Product archived successfully");
    }


    @Operation(
            summary = "Reduce product stock",
            description = "Internal API used by Order Service to reduce product stock after order placement"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock reduced successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity"),
            @ApiResponse(responseCode = "403", description = "Access denied - Only INTERNAL_SERVICE role allowed"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Insufficient stock"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    @PatchMapping("/{id}/reduce-stock")
    public ResponseEntity<String> reduceStock(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Quantity to reduce", example = "2")
            @RequestParam int quantity
    ) {

        service.reduceStock(id, quantity);

        return ResponseEntity.ok("Stock reduced successfully");
    }


}
