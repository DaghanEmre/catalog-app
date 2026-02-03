package com.daghan.catalog.application.port;

import com.daghan.catalog.application.port.paging.PageRequest;
import com.daghan.catalog.application.port.paging.PageResult;
import com.daghan.catalog.domain.model.Product;
import com.daghan.catalog.domain.model.ProductStatus;

import java.util.List;
import java.util.Optional;

/**
 * Persistence Port for Product Aggregate.
 * 
 * Defined in the Application Layer to abstract DB operations.
 * Follows the Dependency Inversion Principle.
 */
public interface ProductRepositoryPort {
    List<Product> findAll();

    Optional<Product> findById(Long id);

    Product save(Product product);

    void deleteById(Long id);

    boolean existsById(Long id);

    /**
     * PR-1 Feature: Search products with server-side pagination and filtering.
     *
     * @param query optional search term (matches product name)
     * @param status optional status filter
     * @param pageRequest pagination parameters
     * @return PageResult with matching products and metadata
     */
    PageResult<Product> search(Optional<String> query, Optional<ProductStatus> status, PageRequest pageRequest);
}
