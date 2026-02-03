package com.daghan.catalog.interfaces.web.rest.dto;

import com.daghan.catalog.domain.model.Product;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stock,
        String status) {
    public static ProductResponse fromDomain(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getStatus().name());
    }

    // MVC template için (Thymeleaf backup)
    public String getFormattedPrice() {
        if (price == null)
            return "0.00";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(price);
    }
}
