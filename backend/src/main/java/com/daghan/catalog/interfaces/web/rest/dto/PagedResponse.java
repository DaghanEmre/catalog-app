package com.daghan.catalog.interfaces.web.rest.dto;

import java.util.List;

/**
 * PR-1 Feature: Server-side pagination response DTO.
 *
 * Returned by GET /api/products/paged endpoint.
 * Wraps paginated items with metadata for client-side virtual scrolling.
 */
public record PagedResponse<T>(
        List<T> items,
        long totalElements,
        int page,
        int size) {

    /**
     * Compact constructor for validation.
     * Called automatically by Java records before assignment.
     */
    public PagedResponse {
        if (items == null) {
            throw new IllegalArgumentException("Items list cannot be null");
        }
    }

    /**
     * Calculates the total number of pages.
     *
     * @return number of pages based on total items and page size
     */
    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    /**
     * Factory method to convert domain PageResult to REST response.
     *
     * @param <D> domain object type
     * @param <T> DTO type
     * @param pageResult domain layer page result
     * @param mapper function to convert domain to DTO
     * @return PagedResponse with mapped items
     */
    public static <D, T> PagedResponse<T> from(
            com.daghan.catalog.application.port.paging.PageResult<D> pageResult,
            java.util.function.Function<D, T> mapper) {
        List<T> dtos = pageResult.items().stream()
                .map(mapper)
                .toList();

        return new PagedResponse<>(
                dtos,
                pageResult.totalElements(),
                pageResult.page(),
                pageResult.size());
    }
}
