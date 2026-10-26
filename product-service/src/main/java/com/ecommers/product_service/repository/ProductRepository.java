package com.ecommers.product_service.repository;

import com.ecommers.product_service.entity.Product;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {

    Page<Product> findByActiveTrue(Pageable pageable);

    Optional<Product> findByIdAndActiveTrue(Long id);

    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(
            String keyword,
            Pageable pageable
    );

    Optional<Product> findBySlugAndActiveTrue(String slug);

    boolean existsBySlug(String slug);

    Optional<Product> findByName(@NotBlank String name);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    UPDATE products
    SET status = CASE
            WHEN stock = :quantity THEN 'OUT_OF_STOCK'
            ELSE status
        END,
        stock = stock - :quantity,
        version = version + 1,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = :id
      AND active = TRUE
      AND status = 'ACTIVE'
      AND stock >= :quantity
    """, nativeQuery = true)
    int reserveStock(
            @Param("id") Long id,
            @Param("quantity") int quantity
    );


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    UPDATE products
    SET stock = stock + :quantity,
        status = CASE
            WHEN status = 'OUT_OF_STOCK' THEN 'ACTIVE'
            ELSE status
        END,
        version = version + 1,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = :id
      AND active = TRUE
    """, nativeQuery = true)
    int releaseStock(
            @Param("id") Long id,
            @Param("quantity") int quantity
    );
}
