package com.daghan.catalog.application.usecase;

import com.daghan.catalog.application.command.CreateProductCommand;
import com.daghan.catalog.application.port.ProductRepositoryPort;
import com.daghan.catalog.domain.model.Product;
import com.daghan.catalog.domain.model.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Primary Use-Case for Product creation.
 * 
 * Orchestrates domain logic and persistence.
 * Defines the transaction boundary for this operation.
 */
@Service
public class CreateProductUseCase {

    private final ProductRepositoryPort repository;

    public CreateProductUseCase(ProductRepositoryPort repository) {
        this.repository = repository;
    }

    @Transactional
    public Product execute(CreateProductCommand command) {
        Product product = Product.create(
                command.name(),
                command.price(),
                command.stock(),
                ProductStatus.fromString(command.status()));
        return repository.save(product);
    }
}
