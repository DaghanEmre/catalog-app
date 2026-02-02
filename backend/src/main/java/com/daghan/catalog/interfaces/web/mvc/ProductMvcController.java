package com.daghan.catalog.interfaces.web.mvc;

import com.daghan.catalog.application.command.CreateProductCommand;
import com.daghan.catalog.application.command.DeleteProductCommand;
import com.daghan.catalog.application.command.UpdateProductCommand;
import com.daghan.catalog.application.dto.ProductResponse;
import com.daghan.catalog.application.usecase.*;
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

@Controller
@RequestMapping("/products")
public class ProductMvcController {

    private static final Logger log = LoggerFactory.getLogger(ProductMvcController.class);

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final ListProductsUseCase listProductsUseCase;

    public ProductMvcController(CreateProductUseCase createProductUseCase,
            UpdateProductUseCase updateProductUseCase,
            DeleteProductUseCase deleteProductUseCase,
            ListProductsUseCase listProductsUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
    }

    @GetMapping
    public String listProducts(Model model) {
        log.debug("Listing all products for MVC view");
        List<ProductResponse> products = listProductsUseCase.execute().stream()
                .map(ProductResponse::fromDomain)
                .toList();

        model.addAttribute("products", products);
        return "products/list";
    }

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
            CreateProductCommand command = new CreateProductCommand(
                    form.name(),
                    form.price(),
                    form.stock(),
                    form.status());

            createProductUseCase.execute(command);
            log.info("Product created successfully: {}", form.name());
            redirectAttributes.addFlashAttribute("success", "Product created successfully!");
        } catch (Exception e) {
            log.error("Failed to create product", e);
            redirectAttributes.addFlashAttribute("error", "Failed to create product: " + e.getMessage());
        }

        return "redirect:/products";
    }

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
            UpdateProductCommand command = new UpdateProductCommand(
                    id,
                    form.name(),
                    form.price(),
                    form.stock(),
                    form.status());

            updateProductUseCase.execute(command);
            log.info("Product updated successfully: {}", id);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        } catch (Exception e) {
            log.error("Failed to update product ID {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Failed to update product: " + e.getMessage());
        }

        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        log.info("Attempting to delete product ID: {}", id);
        try {
            deleteProductUseCase.execute(new DeleteProductCommand(id));
            log.info("Product deleted successfully: {}", id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (Exception e) {
            log.error("Failed to delete product ID {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete product: " + e.getMessage());
        }

        return "redirect:/products";
    }

    public record ProductForm(
            @NotBlank(message = "Product name is required") String name,
            @NotNull(message = "Price is required") @DecimalMin(value = "0.01", message = "Price must be greater than 0") BigDecimal price,
            @NotNull(message = "Stock is required") @Min(value = 0, message = "Stock cannot be negative") Integer stock,
            @NotBlank(message = "Status is required") String status) {
    }
}
