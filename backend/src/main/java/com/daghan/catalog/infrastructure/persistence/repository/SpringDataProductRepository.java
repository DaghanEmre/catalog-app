package com.daghan.catalog.infrastructure.persistence.repository;

import com.daghan.catalog.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByStatus(String status);

    List<ProductEntity> findByNameContainingIgnoreCase(String name);
}
