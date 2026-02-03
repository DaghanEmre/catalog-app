package com.daghan.catalog.infrastructure.persistence.adapter;

import com.daghan.catalog.application.port.ProductRepositoryPort;
import com.daghan.catalog.application.port.paging.PageRequest;
import com.daghan.catalog.application.port.paging.PageResult;
import com.daghan.catalog.domain.exception.ProductNotFoundException;
import com.daghan.catalog.domain.model.Product;
import com.daghan.catalog.domain.model.ProductStatus;
import com.daghan.catalog.infrastructure.persistence.entity.ProductEntity;
import com.daghan.catalog.infrastructure.persistence.repository.SpringDataProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Infrastructure Adapter implementing ProductRepositoryPort.
 * 
 * Bridges the Application Layer's needs with Spring Data JPA.
 * Handles Domain <-> Entity mapping during I/O.
 *
 * PR-1 Update Safety: This adapter fails fast (throws exception) on update of non-existent product.
 * No silent creates allowed.
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
            // Update - CRITICAL FIX: fail fast if product doesn't exist (no silent create!)
            Long id = Objects.requireNonNull(product.getId(), "Product ID cannot be null for update");
            entity = jpaRepository.findById(id)
                    .orElseThrow(() -> new ProductNotFoundException(id)); // ✅ FIXED: was orElseGet
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

    /**
     * PR-1 Feature: Search products with pagination, filtering, and sorting.
     *
     * @param query optional search term (case-insensitive name match)
     * @param status optional status filter
     * @param pageRequest pagination and sort parameters
     * @return PageResult with domain Products
     */
    @Override
    public PageResult<Product> search(Optional<String> query, Optional<ProductStatus> status, PageRequest pageRequest) {
        // Build Spring Data Pageable with sort whitelist validation
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(),
                pageRequest.size(),
                parseSort(pageRequest.sort())
        );

        // Normalize query
        String q = query.map(String::trim).filter(s -> !s.isBlank()).orElse(null);
        String st = status.map(Enum::name).orElse(null);

        // Execute appropriate Spring Data query
        Page<ProductEntity> page;
        if (q != null && st != null) {
            page = jpaRepository.findByNameContainingIgnoreCaseAndStatus(q, st, pageable);
        } else if (q != null) {
            page = jpaRepository.findByNameContainingIgnoreCase(q, pageable);
        } else if (st != null) {
            page = jpaRepository.findByStatus(st, pageable);
        } else {
            page = jpaRepository.findAll(pageable);
        }

        // Map to domain and return
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getTotalElements(),
                pageRequest.page(),
                pageRequest.size()
        );
    }

    /**
     * Sort field whitelist to prevent SQL injection via sort parameter.
     * Only these fields are allowed in ORDER BY clauses.
     */
    private static final Set<String> SORT_WHITELIST = Set.of(
            "id", "name", "price", "stock", "status", "createdAt", "updatedAt"
    );

    /**
     * Parse sort specification string with SQL injection protection.
     *
     * Format: "field1,asc;field2,desc" or null (defaults to "id,asc")
     * Fields not in whitelist are silently ignored.
     *
     * @param sort sort string or null
     * @return Spring Data Sort with validated fields
     */
    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Order.asc("id"));
        }

        Sort result = Sort.unsorted();

        for (String token : sort.split(";")) {
            String[] parts = token.trim().split(",");
            if (parts.length == 0) continue;

            String field = parts[0].trim();
            // Whitelist validation - silently skip unknown fields
            if (!SORT_WHITELIST.contains(field)) continue;

            String direction = (parts.length > 1 ? parts[1].trim().toLowerCase() : "asc");
            Sort.Order order = "desc".equals(direction)
                    ? Sort.Order.desc(field)
                    : Sort.Order.asc(field);

            result = result.and(Sort.by(order));
        }

        // Fallback to default if no valid fields were found
        return result.isUnsorted() ? Sort.by(Sort.Order.asc("id")) : result;
    }
}
