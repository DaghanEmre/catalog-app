package com.daghan.catalog.application.port;

import com.daghan.catalog.domain.model.Product;

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
}
