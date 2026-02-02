package com.daghan.catalog.integration;

import com.daghan.catalog.application.dto.LoginRequest;
import com.daghan.catalog.application.dto.LoginResponse;
import com.daghan.catalog.application.dto.ProductRequest;
import com.daghan.catalog.application.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class ProductApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // Login as admin to get token
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity("/api/auth/login", loginRequest,
                LoginResponse.class);

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        this.adminToken = loginResponse.getBody().token();
    }

    @Test
    void shouldCreateAndListProduct() {
        // 1. Create Product
        ProductRequest request = new ProductRequest("Integrated Product", new BigDecimal("150.00"), 5, "ACTIVE");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductResponse> createResponse = restTemplate.exchange(
                "/api/products",
                HttpMethod.POST,
                entity,
                ProductResponse.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertEquals("Integrated Product", createResponse.getBody().name());

        // 2. List Products
        HttpHeaders listHeaders = new HttpHeaders();
        listHeaders.setBearerAuth(adminToken);
        HttpEntity<Void> listEntity = new HttpEntity<>(listHeaders);

        ResponseEntity<List> listResponse = restTemplate.exchange(
                "/api/products",
                HttpMethod.GET,
                listEntity,
                List.class);

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertFalse(listResponse.getBody().isEmpty());
    }
}
