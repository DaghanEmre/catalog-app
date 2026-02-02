package com.daghan.catalog.infrastructure.persistence.adapter;

import com.daghan.catalog.application.port.ProductRepositoryPort;
import com.daghan.catalog.domain.model.Product;
import com.daghan.catalog.infrastructure.persistence.entity.ProductEntity;
import com.daghan.catalog.infrastructure.persistence.repository.SpringDataProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Infrastructure Adapter implementing ProductRepositoryPort.
 * 
 * Bridges the Application Layer's needs with Spring Data JPA.
 * Handles Domain <-> Entity mapping during I/O.
 */
@Component
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository jpaRepository;
    private final ProductMapper mapper;

    public ProductRepositoryAdapter(SpringDataProductRepository jpaRepository, ProductMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findById(Long id) {
        if (id == null)
            return Optional.empty();
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity;
        if (product.getId() == null) {
            // Create
            entity = mapper.toEntity(product);
        } else {
            // Update
            Long id = product.getId();
            entity = jpaRepository.findById(id)
                    .orElseGet(() -> mapper.toEntity(product));
            mapper.updateEntity(entity, product);
        }

        if (entity == null) {
            throw new IllegalStateException("Entity could not be mapped or found");
        }
        ProductEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            jpaRepository.deleteById(id);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return id != null && jpaRepository.existsById(id);
    }
}
