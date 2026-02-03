package com.daghan.catalog.interfaces.web.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required") String name,

        @NotNull(message = "Price is required") @DecimalMin(value = "0.01", message = "Price must be greater than 0") BigDecimal price,

        @NotNull(message = "Stock is required") @Min(value = 0, message = "Stock cannot be negative") Integer stock,

        @NotBlank(message = "Status is required") String status) {
}
