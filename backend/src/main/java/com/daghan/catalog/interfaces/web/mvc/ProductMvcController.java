package com.daghan.catalog.interfaces.web.mvc;

import com.daghan.catalog.application.dto.ProductResponse;
import com.daghan.catalog.infrastructure.persistence.entity.ProductEntity;
import com.daghan.catalog.infrastructure.persistence.repository.SpringDataProductRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for Product Management Web UI
 * Handles CRUD operations for products in the browser
 */
@Controller
@RequestMapping("/products")
public class ProductMvcController {

    private static final Logger log = LoggerFactory.getLogger(ProductMvcController.class);
    private final SpringDataProductRepository productRepository;

    public ProductMvcController(SpringDataProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * List all products - Accessible by authenticated users
     * Uses DTO to avoid entity leakage to view layer
     */
    @GetMapping
    public String listProducts(Model model) {
        log.debug("Listing all products for MVC view");
        List<ProductResponse> products = productRepository.findAll()
                .stream()
                .map(ProductResponse::fromEntity)
                .toList();

        model.addAttribute("products", products);
        return "products/list";
    }

    /**
     * Create new product - Admin only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createProduct(
            @Valid @ModelAttribute ProductForm form,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            log.warn("Validation failed for product creation: {}", result.getAllErrors());
            redirectAttributes.addFlashAttribute("error", "Invalid product data. Please check your inputs.");
            return "redirect:/products";
        }

        try {
            ProductEntity entity = new ProductEntity(
                    form.name(),
                    form.price(),
                    form.stock(),
                    form.status());

            productRepository.save(entity);
            log.info("Product created successfully: {}", form.name());
            redirectAttributes.addFlashAttribute("success", "Product created successfully!");
        } catch (Exception e) {
            log.error("Failed to create product", e);
            redirectAttributes.addFlashAttribute("error", "Failed to create product. System error occurred.");
        }

        return "redirect:/products";
    }

    /**
     * Update existing product - Admin only
     * Uses POST for compatibility with simple HTML forms (no method override
     * needed)
     */
    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductForm form,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            log.warn("Validation failed for product update ID {}: {}", id, result.getAllErrors());
            redirectAttributes.addFlashAttribute("error", "Invalid product data.");
            return "redirect:/products";
        }

        try {
            ProductEntity entity = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

            entity.setName(form.name());
            entity.setPrice(form.price());
            entity.setStock(form.stock());
            entity.setStatus(form.status());

            productRepository.save(entity);
            log.info("Product updated successfully: {}", id);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        } catch (Exception e) {
            log.error("Failed to update product ID {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Failed to update product.");
        }

        return "redirect:/products";
    }

    /**
     * Delete product - Admin only
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        log.info("Attempting to delete product ID: {}", id);
        try {
            if (!productRepository.existsById(id)) {
                log.warn("Delete failed: Product not found ID {}", id);
                redirectAttributes.addFlashAttribute("error", "Product not found.");
                return "redirect:/products";
            }

            productRepository.deleteById(id);
            log.info("Product deleted successfully: {}", id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (Exception e) {
            log.error("Failed to delete product ID {}", id, e);
            redirectAttributes.addFlashAttribute("error",
                    "Failed to delete product. It might be referenced by other data.");
        }

        return "redirect:/products";
    }

    /**
     * Form DTO for product creation/update
     * Separate from API DTO to handle form-specific validation and web context
     */
    public record ProductForm(
            @NotBlank(message = "Product name is required") String name,

            @NotNull(message = "Price is required") @DecimalMin(value = "0.01", message = "Price must be greater than 0") BigDecimal price,

            @NotNull(message = "Stock is required") @Min(value = 0, message = "Stock cannot be negative") Integer stock,

            @NotBlank(message = "Status is required") String status) {
    }
}
