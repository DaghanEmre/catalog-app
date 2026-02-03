package com.daghan.catalog.interfaces.web.rest;

import com.daghan.catalog.application.command.CreateProductCommand;
import com.daghan.catalog.application.command.DeleteProductCommand;
import com.daghan.catalog.application.command.UpdateProductCommand;
import com.daghan.catalog.application.port.paging.PageRequest;
import com.daghan.catalog.application.port.paging.PageResult;
import com.daghan.catalog.application.usecase.*;
import com.daghan.catalog.domain.model.Product;
import com.daghan.catalog.domain.model.ProductStatus;
import com.daghan.catalog.interfaces.web.rest.dto.PagedResponse;
import com.daghan.catalog.interfaces.web.rest.dto.ProductRequest;
import com.daghan.catalog.interfaces.web.rest.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST API Adapter for Product management.
 * 
 * Communicates solely via Use-Cases (Application Layer).
 * Handles DTO-to-Command mapping and Domain-to-Response mapping.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ProductRestController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final GetProductUseCase getProductUseCase;
    private final SearchProductsPageUseCase searchProductsPageUseCase;

    public ProductRestController(CreateProductUseCase createProductUseCase,
            UpdateProductUseCase updateProductUseCase,
            DeleteProductUseCase deleteProductUseCase,
            ListProductsUseCase listProductsUseCase,
            GetProductUseCase getProductUseCase,
            SearchProductsPageUseCase searchProductsPageUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.getProductUseCase = getProductUseCase;
        this.searchProductsPageUseCase = searchProductsPageUseCase;
    }

    @GetMapping
    @Operation(summary = "List all products", description = "Get all products (authenticated users)")
    public ResponseEntity<List<ProductResponse>> listProducts() {
        List<ProductResponse> products = listProductsUseCase.execute().stream()
                .map(ProductResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Get a single product by its ID")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable @NonNull Long id) {
        Product product = getProductUseCase.execute(id);
        return ResponseEntity.ok(ProductResponse.fromDomain(product));
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product (admin only)")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
        CreateProductCommand command = new CreateProductCommand(
                request.name(),
                request.price(),
                request.stock(),
                request.status());

        Product product = createProductUseCase.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductResponse.fromDomain(product));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product (admin only)")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable @NonNull Long id,
            @RequestBody @Valid ProductRequest request) {

        UpdateProductCommand command = new UpdateProductCommand(
                id,
                request.name(),
                request.price(),
                request.stock(),
                request.status());

        Product product = updateProductUseCase.execute(command);
        return ResponseEntity.ok(ProductResponse.fromDomain(product));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product (admin only)")
    public ResponseEntity<Void> deleteProduct(@PathVariable @NonNull Long id) {
        deleteProductUseCase.execute(new DeleteProductCommand(id));
        return ResponseEntity.noContent().build();
    }

    /**
     * PR-1 Feature: Server-side pagination endpoint.
     *
     * @param q optional search query (matches product name)
     * @param status optional product status filter
     * @param page page index (0-based)
     * @param size items per page (1-200)
     * @param sort sort specification (e.g., "name,asc")
     * @return paginated products with metadata
     */
    @GetMapping("/paged")
    @Operation(summary = "List products (paged)", description = "Get products with server-side pagination and filtering")
    public ResponseEntity<PagedResponse<ProductResponse>> pagedProducts(
            @RequestParam Optional<String> q,
            @RequestParam Optional<ProductStatus> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String sort) {

        PageRequest pageRequest = PageRequest.of(page, size, sort != null ? sort : "id,asc");
        PageResult<Product> result = searchProductsPageUseCase.execute(q, status, pageRequest);

        return ResponseEntity.ok(PagedResponse.from(result, ProductResponse::fromDomain));
    }
}
