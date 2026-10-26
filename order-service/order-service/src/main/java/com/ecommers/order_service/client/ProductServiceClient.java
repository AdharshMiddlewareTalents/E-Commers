package com.ecommers.order_service.client;


import com.ecommers.order_service.config.ProductServiceFeignConfig;
import com.ecommers.order_service.dto.Response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "product-service",
        url = "${product-service.url}",
        configuration = ProductServiceFeignConfig.class
)
public interface ProductServiceClient {

    @GetMapping("/internal/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);

    @PatchMapping("/internal/products/{id}/reserve-stock")
    void reserveStock(
            @PathVariable("id") Long id,
            @RequestParam("quantity") int quantity
    );

    @PatchMapping("/internal/products/{id}/release-stock")
    void releaseStock(
            @PathVariable("id") Long id,
            @RequestParam("quantity") int quantity
    );
}
