package com.ecommers.product_service.service;

import com.ecommers.product_service.Exception.BadRequestException;
import com.ecommers.product_service.Exception.ResourceNotFoundException;
import com.ecommers.product_service.dto.CreateProductRequest;
import com.ecommers.product_service.dto.ProductResponse;
import com.ecommers.product_service.dto.UpdateProductRequest;
import com.ecommers.product_service.entity.Product;
import com.ecommers.product_service.entity.ProductStatus;
import com.ecommers.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "products")
public class ProductService {

    private final ProductRepository productRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public ProductResponse createProduct(
            CreateProductRequest request,
            String userEmail) throws IOException {

        log.info("===CREATE PRODUCT START===");

        log.info("Request recived: name={},price{},stock{},category{},description{}",
                request.getName(),
                request.getStock(),
                request.getPrice(),
                request.getCategory(),
                request.getDescription());

        String slug = generateUniqueSlug(request.getName());

        log.info("Generated slug:{}",slug);

        if(productRepository.findBySlugAndActiveTrue(slug).isPresent()){
            throw new ResourceNotFoundException("Product already exists");
        }

        log.info("Setting product status: {}", ProductStatus.ACTIVE);


        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .status(ProductStatus.ACTIVE)
                .active(true)
                .createdBy(userEmail)
                .updatedBy(userEmail)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        log.info("Product before save: {}", product);

         Product savedProduct = productRepository.save(product);

        log.info("Product after save: {}", savedProduct);
        log.info("Saved slug: {}", savedProduct.getSlug());
        log.info("Saved status: {}", savedProduct.getStatus());


        log.info("=== CREATE PRODUCT END ===");

        return mapToResponse(product);
    }

    @Transactional
    @Caching(evict = {
           @CacheEvict(key = "#id"),
           @CacheEvict(key = "#result.slug",condition = "#result!=null")
   })
    public ProductResponse update(
            Long id,
            UpdateProductRequest request,
            String userEmail
    ){
        Product product = productRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException("Product not found"));

        if (request.getName()!=null){
            product.setName(request.getName());
            product.setSlug(generateUniqueSlug(request.getName()));
        }

        if(request.getDescription()!=null){
            product.setDescription(request.getDescription());
        }

        if (request.getPrice() != null)
            product.setPrice(request.getPrice());

        if (request.getStock() != null)
            product.setStock(request.getStock());

        if (request.getCategory() != null)
            product.setCategory(request.getCategory());

        product.setUpdatedBy(userEmail);
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);

        return mapToResponse(product);
    }



    public Page<ProductResponse> getALl(
            String keyword,
            Pageable pageable
    ){
        int safeSize = Math.min(pageable.getPageSize(),50);
        Pageable safePageable =
                PageRequest.of(pageable.getPageNumber(),safeSize);

        Page<Product> page;

        if (keyword!=null && !keyword.isEmpty()){
            page = productRepository
                    .findByNameContainingIgnoreCaseAndActiveTrue(
                            keyword,safePageable
                    );
        }else {
            page = productRepository.findByActiveTrue(safePageable);
        }

        return page.map(this::mapToResponse);
    }

    @Cacheable(key = "#id")
    public ProductResponse getProudutById(Long id){

        Product product = productRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException("Product not found"));

        return mapToResponse(product);
    }

    @Cacheable(key = "#slug")
    public ProductResponse getBySlug(String slug){
        Product product = productRepository
                .findBySlugAndActiveTrue(slug)
                .orElseThrow(()->
                        new ResourceNotFoundException("Product not found"));
        return mapToResponse(product);
    }

    @Transactional
    @CacheEvict(key = "#id")
    public void changeStatus(Long id,
                             ProductStatus status,
                             String userEmail){
        Product product = productRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException("Product not found"));
        product.setStatus(status);
        product.setUpdatedBy(userEmail);
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);
    }


    @Transactional
    @CacheEvict(key = "#id")
    public void delete(Long id,String userEmail){
        Product product = productRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException("Product not found"));

        product.setStatus(ProductStatus.ARCHIVED);
        product.setActive(false);
        product.setUpdatedBy(userEmail);
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);

    }


    @Transactional
    @CacheEvict(key = "#id")
    public void reduceStock(Long id, int quantity ){
        Product product = productRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException("product not found"));
        if (product.getStock()<quantity){
            throw new BadRequestException("insufficient stock");
        }

        product.setStock(product.getStock() - quantity);

        if (product.getStock() == 0){
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }

        productRepository.save(product);
    }


    private String saveImage(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) return null;

        File directory = new File(uploadDir);
        if (!directory.exists()) directory.mkdirs();

        String fileName =
                UUID.randomUUID() + ".png";

        Path path = Paths.get(uploadDir + fileName);
        Files.write(path, file.getBytes());

        return "/images/" + fileName;
    }

    private void validateImage(MultipartFile file){

        if(file == null || file.isEmpty()) return;

        if(!file.getContentType().startsWith("image/")){
            throw new BadRequestException("Inavlide file type");
        }

        if(file.getSize()>5*1024*1024){
            throw new BadRequestException("File is to large");
        }
    }

    private String generateUniqueSlug(String name) {

        String baseSlug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .trim();

        String slug = baseSlug;
        int count = 1;

        while (productRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + count++;
        }

        return slug;
    }


    private ProductResponse mapToResponse(Product product){
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .slug(product.getSlug())
                .status(product.getStatus())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .build();
    }

}
