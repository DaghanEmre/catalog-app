package com.daghan.catalog.infrastructure.persistence;

import com.daghan.catalog.infrastructure.persistence.entity.ProductEntity;
import com.daghan.catalog.infrastructure.persistence.entity.UserEntity;
import com.daghan.catalog.infrastructure.persistence.repository.SpringDataProductRepository;
import com.daghan.catalog.infrastructure.persistence.repository.SpringDataUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final SpringDataUserRepository userRepository;
    private final SpringDataProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            SpringDataUserRepository userRepository,
            SpringDataProductRepository productRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedProducts();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            log.info("Seeding initial users...");

            userRepository.save(new UserEntity(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "ROLE_ADMIN"));

            userRepository.save(new UserEntity(
                    "user",
                    passwordEncoder.encode("user123"),
                    "ROLE_USER"));

            log.info("✓ Users seeded successfully");
        } else {
            log.info("Users already exist, skipping seed");
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            log.info("Seeding sample products...");

            productRepository.save(new ProductEntity(
                    "Laptop Dell XPS 15",
                    new BigDecimal("1299.99"),
                    15,
                    "ACTIVE"));

            productRepository.save(new ProductEntity(
                    "iPhone 15 Pro",
                    new BigDecimal("999.00"),
                    25,
                    "ACTIVE"));

            productRepository.save(new ProductEntity(
                    "Sony WH-1000XM5",
                    new BigDecimal("349.99"),
                    40,
                    "ACTIVE"));

            productRepository.save(new ProductEntity(
                    "iPad Pro 12.9",
                    new BigDecimal("1099.00"),
                    10,
                    "ACTIVE"));

            productRepository.save(new ProductEntity(
                    "Samsung Galaxy S24",
                    new BigDecimal("799.99"),
                    0,
                    "DISCONTINUED"));

            log.info("✓ Products seeded successfully");
        } else {
            log.info("Products already exist, skipping seed");
        }
    }
}
