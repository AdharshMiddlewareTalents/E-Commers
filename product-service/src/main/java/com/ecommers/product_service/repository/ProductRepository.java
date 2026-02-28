package com.ecommers.product_service.repository;

import com.ecommers.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(
            String keyword,
            Pageable pageable
    );

    Optional<Product> findBySlugAndActiveTrue(String slug);

    boolean existsBySlug(String slug);
}
