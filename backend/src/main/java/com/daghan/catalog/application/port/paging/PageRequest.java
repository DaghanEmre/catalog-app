package com.daghan.catalog.application.port.paging;

/**
 * PR-1 Feature: Pagination request parameters wrapper.
 *
 * Encapsulates pagination metadata passed from client to backend.
 * Validates page index and size constraints to prevent abuse.
 */
public record PageRequest(int page, int size, String sort) {

    /**
     * Compact constructor for validation.
     * Called automatically by Java records before assignment.
     *
     * @throws IllegalArgumentException if page or size violate constraints
     */
    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must be >= 0, got: " + page);
        }
        if (size <= 0 || size > 200) {
            throw new IllegalArgumentException("Page size must be between 1 and 200, got: " + size);
        }
    }

    /**
     * Factory method for creating paginated requests.
     *
     * @param page page number (0-indexed)
     * @param size items per page
     * @param sort sort specification (e.g., "name,asc")
     * @return new PageRequest with validated parameters
     */
    public static PageRequest of(int page, int size, String sort) {
        return new PageRequest(page, size, sort);
    }

    /**
     * Creates a default pagination request.
     *
     * @return PageRequest with page=0, size=20, sort="id,asc"
     */
    public static PageRequest defaultRequest() {
        return new PageRequest(0, 20, "id,asc");
    }
}
