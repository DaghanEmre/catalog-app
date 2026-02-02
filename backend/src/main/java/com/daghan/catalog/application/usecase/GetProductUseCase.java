package com.daghan.catalog.application.usecase;

import com.daghan.catalog.application.port.ProductRepositoryPort;
import com.daghan.catalog.domain.exception.ProductNotFoundException;
import com.daghan.catalog.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetProductUseCase {

    private final ProductRepositoryPort repository;

    public GetProductUseCase(ProductRepositoryPort repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Product execute(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
