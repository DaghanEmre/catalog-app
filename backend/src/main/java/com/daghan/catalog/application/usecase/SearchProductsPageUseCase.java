package com.daghan.catalog.application.usecase;

import com.daghan.catalog.application.port.ProductRepositoryPort;
import com.daghan.catalog.application.port.paging.PageRequest;
import com.daghan.catalog.application.port.paging.PageResult;
import com.daghan.catalog.domain.model.Product;
import com.daghan.catalog.domain.model.ProductStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * PR-1 Feature: Search and paginate products.
 *
 * Use-case for server-side paged product search with filtering.
 * Returns domain Product objects (not DTOs) per Clean Architecture.
 */
@Service
public class SearchProductsPageUseCase {
    private static final Logger logger = LoggerFactory.getLogger(SearchProductsPageUseCase.class);

    private final ProductRepositoryPort repository;

    public SearchProductsPageUseCase(ProductRepositoryPort repository) {
        this.repository = repository;
    }

    /**
     * Search products with pagination and optional filtering.
     *
     * Transaction is read-only for performance (no writes expected).
     *
     * @param query optional search term (matches product name)
     * @param status optional product status filter
     * @param pageRequest pagination parameters (page, size, sort)
     * @return PageResult containing matching products and metadata
     */
    @Transactional(readOnly = true)
    public PageResult<Product> execute(
            Optional<String> query,
            Optional<ProductStatus> status,
            PageRequest pageRequest) {

        logger.debug("Searching products: query={}, status={}, page={}, size={}, sort={}",
                query.map(q -> "'" + q + "'").orElse("*"),
                status.map(Enum::name).orElse("*"),
                pageRequest.page(),
                pageRequest.size(),
                pageRequest.sort());

        PageResult<Product> result = repository.search(query, status, pageRequest);

        logger.debug("Search completed: found {} of {} total products",
                result.items().size(),
                result.totalElements());

        return result;
    }
}
