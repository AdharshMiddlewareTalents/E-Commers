package com.ecommers.product_service.controller;

import com.ecommers.product_service.dto.ProductResponse;
import com.ecommers.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProudutById(id);
    }

    @PatchMapping("/{id}/reserve-stock")
    public void reserveStock(
            @PathVariable Long id,
            @RequestParam int quantity) {

        productService.reserveStock(id, quantity);
    }

    @PatchMapping("/{id}/release-stock")
    public void releaseStock(
            @PathVariable Long id,
            @RequestParam int quantity) {

        productService.releaseStock(id, quantity);
    }

}
