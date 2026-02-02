package com.daghan.catalog.domain.model;

import com.daghan.catalog.domain.exception.InvalidProductInputException;

public enum ProductStatus {
    ACTIVE,
    DISCONTINUED;

    public static ProductStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVE;
        }
        try {
            return ProductStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new InvalidProductInputException(
                    "Invalid product status: '" + value + "'. Valid values are: ACTIVE, DISCONTINUED");
        }
    }
}
