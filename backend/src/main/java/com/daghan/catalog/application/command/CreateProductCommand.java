package com.daghan.catalog.application.command;

import java.math.BigDecimal;

public record CreateProductCommand(
        String name,
        BigDecimal price,
        Integer stock,
        String status) {
}
