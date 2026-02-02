package com.daghan.catalog.application.command;

import java.math.BigDecimal;

public record UpdateProductCommand(
        Long id,
        String name,
        BigDecimal price,
        Integer stock,
        String status) {
}
