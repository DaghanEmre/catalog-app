package com.daghan.catalog.application.usecase;

import com.daghan.catalog.application.command.DeleteProductCommand;
import com.daghan.catalog.application.port.ProductRepositoryPort;
import com.daghan.catalog.domain.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteProductUseCase {

    private final ProductRepositoryPort repository;

    public DeleteProductUseCase(ProductRepositoryPort repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(DeleteProductCommand command) {
        if (!repository.existsById(command.id())) {
            throw new ProductNotFoundException(command.id());
        }
        repository.deleteById(command.id());
    }
}
