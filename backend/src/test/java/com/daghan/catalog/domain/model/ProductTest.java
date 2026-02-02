package com.daghan.catalog.domain.model;

import com.daghan.catalog.domain.exception.InvalidProductInputException;
import com.daghan.catalog.domain.exception.InvalidProductStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateValidProduct() {
        Product product = Product.create("Test Product", new BigDecimal("99.99"), 10, ProductStatus.ACTIVE);

        assertNotNull(product);
        assertEquals("Test Product", product.getName());
        assertEquals(new BigDecimal("99.99"), product.getPrice());
        assertEquals(10, product.getStock());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(InvalidProductInputException.class,
                () -> Product.create("", new BigDecimal("10.00"), 5, ProductStatus.ACTIVE));
    }

    @Test
    void shouldThrowExceptionWhenPriceIsZeroOrNegative() {
        assertThrows(InvalidProductInputException.class,
                () -> Product.create("Test", BigDecimal.ZERO, 5, ProductStatus.ACTIVE));
        assertThrows(InvalidProductInputException.class,
                () -> Product.create("Test", new BigDecimal("-1.00"), 5, ProductStatus.ACTIVE));
    }

    @Test
    void shouldThrowExceptionWhenReactivatingDiscontinuedProduct() {
        Product product = Product.reconstruct(1L, "Test", new BigDecimal("10.00"), 5, ProductStatus.DISCONTINUED);

        assertThrows(InvalidProductStateException.class, () -> product.changeStatus(ProductStatus.ACTIVE));
    }

    @Test
    void shouldAllowUpdatingPriceAndStock() {
        Product product = Product.create("Test", new BigDecimal("10.00"), 5, ProductStatus.ACTIVE);

        product.updatePrice(new BigDecimal("20.00"));
        product.adjustStock(15);

        assertEquals(new BigDecimal("20.00"), product.getPrice());
        assertEquals(15, product.getStock());
    }
}
