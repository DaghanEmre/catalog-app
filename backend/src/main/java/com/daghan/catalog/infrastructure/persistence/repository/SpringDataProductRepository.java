package com.daghan.catalog.infrastructure.persistence.repository;

import com.daghan.catalog.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByStatus(String status);

    List<ProductEntity> findByNameContainingIgnoreCase(String name);

    /**
     * PR-1 Feature: Find products by name (case-insensitive) with paging.
     * Used by server-side search endpoint.
     */
    Page<ProductEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * PR-1 Feature: Find products by status with paging.
     * Used by server-side filter endpoint.
     */
    Page<ProductEntity> findByStatus(String status, Pageable pageable);

    /**
     * PR-1 Feature: Find products by name AND status with paging.
     * Used by combined search + filter.
     */
    Page<ProductEntity> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);
}
