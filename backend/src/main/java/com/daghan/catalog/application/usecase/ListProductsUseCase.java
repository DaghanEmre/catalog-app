package com.daghan.catalog.application.usecase;

import com.daghan.catalog.application.port.ProductRepositoryPort;
import com.daghan.catalog.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListProductsUseCase {

    private final ProductRepositoryPort repository;

    public ListProductsUseCase(ProductRepositoryPort repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Product> execute() {
        return repository.findAll();
    }
}
