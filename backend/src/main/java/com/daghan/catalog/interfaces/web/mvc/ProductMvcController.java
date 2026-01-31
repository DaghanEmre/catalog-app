package com.daghan.catalog.interfaces.web.mvc;

import com.daghan.catalog.infrastructure.persistence.repository.SpringDataProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products")
public class ProductMvcController {

    private final SpringDataProductRepository productRepository;

    public ProductMvcController(SpringDataProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public String listProducts(Model model) {
        var products = productRepository.findAll();
        model.addAttribute("products", products);
        return "products/list";
    }
}
