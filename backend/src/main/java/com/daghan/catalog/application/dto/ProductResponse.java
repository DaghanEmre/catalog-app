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

    /**
     * Helper for consistent price formatting in UI templates
     * Prevents Thymeleaf version compatibility issues with #numbers.formatDecimal
     */
    public String getFormattedPrice() {
        return price != null ? String.format("%.2f", price) : "0.00";
    }

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
