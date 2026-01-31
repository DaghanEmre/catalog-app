package com.daghan.catalog.application.dto;

import com.daghan.catalog.infrastructure.persistence.entity.ProductEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stock,
        String status,
        Instant createdAt,
        Instant updatedAt) {
    public static ProductResponse fromEntity(ProductEntity entity) {
        return new ProductResponse(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getStock(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
