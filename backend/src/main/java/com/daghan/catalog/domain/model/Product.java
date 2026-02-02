package com.daghan.catalog.domain.model;

import com.daghan.catalog.domain.exception.InvalidProductInputException;
import com.daghan.catalog.domain.exception.InvalidProductStateException;

import java.math.BigDecimal;

/**
 * Product Domain Aggregate.
 * 
 * Enforces business invariants and state transitions.
 * Pure Java (POJO) - independent of any framework or persistence logic.
 */
public final class Product {
    private final Long id;
    private String name;
    private BigDecimal price;
    private int stock;
    private ProductStatus status;

    private Product(Long id, String name, BigDecimal price, int stock, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }

    /**
     * Factory method for creating a new product instance.
     * Enforces creation-time invariants (name, price, stock).
     * 
     * @param name   Name of the product (required)
     * @param price  Positive price amount
     * @param stock  Non-negative initial stock
     * @param status Initial status (defaults to ACTIVE if null)
     * @return A valid Product instance
     */
    public static Product create(String name, BigDecimal price, Integer stock, ProductStatus status) {
        validateName(name);
        validatePrice(price);
        validateStock(stock);
        return new Product(null, name.trim(), price, stock, status != null ? status : ProductStatus.ACTIVE);
    }

    /**
     * Reconstructs an existing product instance from persistence.
     * Used by mappers to reload state without triggering new-creation logic.
     */
    public static Product reconstruct(Long id, String name, BigDecimal price, Integer stock, ProductStatus status) {
        if (id == null) {
            throw new InvalidProductInputException("ID required for reconstruction");
        }
        validateName(name);
        validatePrice(price);
        validateStock(stock);
        return new Product(id, name.trim(), price, stock, status);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductInputException("Name is required");
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductInputException("Price must be positive");
        }
    }

    private static void validateStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new InvalidProductInputException("Stock cannot be negative");
        }
    }

    public void rename(String newName) {
        validateName(newName);
        this.name = newName.trim();
    }

    public void updatePrice(BigDecimal newPrice) {
        validatePrice(newPrice);
        this.price = newPrice;
    }

    public void adjustStock(int newStock) {
        validateStock(newStock);
        this.stock = newStock;
    }

    /**
     * Business Rule: Handles status transitions.
     * Prevents reactivating a DISCONTINUED product as per business requirements.
     */
    public void changeStatus(ProductStatus newStatus) {
        if (this.status == ProductStatus.DISCONTINUED && newStatus == ProductStatus.ACTIVE) {
            throw new InvalidProductStateException("Cannot reactivate discontinued product");
        }
        this.status = newStatus;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public ProductStatus getStatus() {
        return status;
    }
}
