package com.daghan.catalog.application.port.paging;

import java.util.List;

/**
 * PR-1 Feature: Pagination result wrapper with metadata.
 *
 * Immutable record encapsulating paginated query results and pagination metadata.
 * Returned by repository search operations.
 */
public record PageResult<T>(
        List<T> items,
        long totalElements,
        int page,
        int size) {

    /**
     * Compact constructor for validation.
     * Called automatically by Java records before assignment.
     *
     * @throws IllegalArgumentException if items is null or totalElements is negative
     */
    public PageResult {
        if (items == null) {
            throw new IllegalArgumentException("Items list cannot be null");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("Total elements cannot be negative, got: " + totalElements);
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
     * Checks if there's a next page available.
     *
     * @return true if current page < total pages
     */
    public boolean hasNext() {
        return page < totalPages() - 1;
    }

    /**
     * Checks if there's a previous page available.
     *
     * @return true if current page > 0
     */
    public boolean hasPrevious() {
        return page > 0;
    }
}
