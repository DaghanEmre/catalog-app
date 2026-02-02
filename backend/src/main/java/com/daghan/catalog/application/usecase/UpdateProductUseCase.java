package com.daghan.catalog.application.usecase;

import com.daghan.catalog.application.command.UpdateProductCommand;
import com.daghan.catalog.application.port.ProductRepositoryPort;
import com.daghan.catalog.domain.exception.ProductNotFoundException;
import com.daghan.catalog.domain.model.Product;
import com.daghan.catalog.domain.model.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProductUseCase {

    private final ProductRepositoryPort repository;

    public UpdateProductUseCase(ProductRepositoryPort repository) {
        this.repository = repository;
    }

    @Transactional
    public Product execute(UpdateProductCommand command) {
        Product product = repository.findById(command.id())
                .orElseThrow(() -> new ProductNotFoundException(command.id()));

        product.rename(command.name());
        product.updatePrice(command.price());
        product.adjustStock(command.stock());
        product.changeStatus(ProductStatus.fromString(command.status()));

        return repository.save(product);
    }
}
