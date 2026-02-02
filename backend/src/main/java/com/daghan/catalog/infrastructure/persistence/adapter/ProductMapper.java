package com.daghan.catalog.infrastructure.persistence.adapter;

import com.daghan.catalog.domain.model.Product;
import com.daghan.catalog.domain.model.ProductStatus;
import com.daghan.catalog.infrastructure.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toDomain(ProductEntity entity) {
        return Product.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getStock(),
                ProductStatus.fromString(entity.getStatus()));
    }

    public ProductEntity toEntity(Product domain) {
        ProductEntity entity = new ProductEntity(
                domain.getName(),
                domain.getPrice(),
                domain.getStock(),
                domain.getStatus().name());
        // Note: ID will be null for new entities, and set via setId if it exists
        // (reconstruction)
        return entity;
    }

    public void updateEntity(ProductEntity entity, Product domain) {
        entity.setName(domain.getName());
        entity.setPrice(domain.getPrice());
        entity.setStock(domain.getStock());
        entity.setStatus(domain.getStatus().name());
    }
}
