package com.daghan.catalog.interfaces.web.rest;

import com.daghan.catalog.application.dto.ProductRequest;
import com.daghan.catalog.application.dto.ProductResponse;
import com.daghan.catalog.infrastructure.persistence.entity.ProductEntity;
import com.daghan.catalog.infrastructure.persistence.repository.SpringDataProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ProductRestController {

    private final SpringDataProductRepository productRepository;

    public ProductRestController(SpringDataProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    @Operation(summary = "List all products", description = "Get all products (authenticated users)")
    public ResponseEntity<List<ProductResponse>> listProducts() {
        List<ProductResponse> products = productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Get a single product by its ID")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable @NonNull Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product (admin only)")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
        ProductEntity entity = new ProductEntity(
                request.name(),
                request.price(),
                request.stock(),
                request.status());

        ProductEntity saved = productRepository.save(entity);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductResponse.fromEntity(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product (admin only)")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable @NonNull Long id,
            @RequestBody @Valid ProductRequest request) {
        return productRepository.findById(id)
                .map(entity -> {
                    entity.setName(request.name());
                    entity.setPrice(request.price());
                    entity.setStock(request.stock());
                    entity.setStatus(request.status());

                    ProductEntity saved = productRepository.save(entity);
                    return ResponseEntity.ok(ProductResponse.fromEntity(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product (admin only)")
    public ResponseEntity<Void> deleteProduct(@PathVariable @NonNull Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
