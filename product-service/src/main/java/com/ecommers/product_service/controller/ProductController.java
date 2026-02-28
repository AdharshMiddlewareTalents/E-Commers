package com.ecommers.product_service.controller;

import com.ecommers.product_service.dto.CreateProductRequest;
import com.ecommers.product_service.dto.ProductResponse;
import com.ecommers.product_service.dto.UpdateProductRequest;
import com.ecommers.product_service.entity.ProductStatus;
import com.ecommers.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(
            @RequestBody @Valid CreateProductRequest request,
            Authentication authentication
    ) throws IOException {
        System.out.println("Enterd Controller");
        String email = authentication.getName();

        ProductResponse response = service.createProduct(request,email);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProductRequest request,
            Authentication authentication
    ){
        String email = authentication.getName();

        return ResponseEntity.ok(
                service.update(id,request,email)
        );
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ){
        Page<ProductResponse> responses=
                service.getALl(keyword, pageable);

       return ResponseEntity.ok(responses);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.getProudutById(id)
        );
    }


    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getBySlug(
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(
                service.getBySlug(slug)
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> changeStatus(
            @PathVariable Long id,
            @RequestParam ProductStatus status,
            Authentication authentication
    ) {

        String email = authentication.getName();

        service.changeStatus(id, status, email);

        return ResponseEntity.ok("Product status updated");
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        service.delete(id, email);

        return ResponseEntity.ok("Product archived successfully");
    }


    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    @PatchMapping("/{id}/reduce-stock")
    public ResponseEntity<String> reduceStock(
            @PathVariable Long id,
            @RequestParam int quantity
    ) {

        service.reduceStock(id, quantity);

        return ResponseEntity.ok("Stock reduced successfully");
    }


}
