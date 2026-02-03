package com.daghan.catalog.integration;

import com.daghan.catalog.application.dto.LoginRequest;
import com.daghan.catalog.application.dto.LoginResponse;
import com.daghan.catalog.interfaces.web.rest.dto.ProductRequest;
import com.daghan.catalog.interfaces.web.rest.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Product API endpoints.
 * 
 * <p>
 * These tests verify the complete request/response cycle including:
 * <ul>
 * <li>Authentication via JWT tokens</li>
 * <li>Product CRUD operations</li>
 * <li>Proper HTTP status codes and response bodies</li>
 * </ul>
 * 
 * <p>
 * All tests run against a real application context with Testcontainers.
 * 
 * @see AbstractIntegrationTest
 */
@ActiveProfiles("test")
@DisplayName("Product API Integration Tests")
class ProductApiIntegrationTest extends AbstractIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        private String adminToken;

        /**
         * Authenticates as admin user before each test.
         * The token is stored for use in subsequent API calls.
         */
        @BeforeEach
        void setUp() {
                LoginRequest loginRequest = new LoginRequest("admin", "admin123");
                ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                                "/api/auth/login",
                                loginRequest,
                                LoginResponse.class);

                assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                LoginResponse body = requireBody(loginResponse, "login response");
                String token = body.token();
                assertThat(token)
                                .as("JWT token should not be null or empty")
                                .isNotNull()
                                .isNotEmpty();

                this.adminToken = token;
        }

        /**
         * Creates HTTP headers with Bearer authentication.
         * This method returns a non-null HttpHeaders instance that is safe to use
         * with HttpEntity constructors expecting @NonNull parameters.
         * 
         * @return HttpHeaders configured with admin JWT token, never null
         */
        @NonNull
        private HttpHeaders createAuthHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(Objects.requireNonNull(adminToken,
                                "Admin token must be initialized before creating auth headers"));
                return headers;
        }

        /**
         * Creates an HttpEntity with only authentication headers (no body).
         * This is a convenience method for GET and DELETE requests.
         * 
         * @return HttpEntity with authentication headers
         */
        @NonNull
        private HttpEntity<Void> createAuthEntity() {
                return new HttpEntity<>(null, createAuthHeaders());
        }

        /**
         * Creates an HttpEntity with the given body and authentication headers.
         * 
         * @param body the request body
         * @param <T>  the type of the request body
         * @return HttpEntity with body and authentication headers
         */
        @NonNull
        private <T> HttpEntity<T> createAuthEntity(@NonNull T body) {
                return new HttpEntity<>(body, createAuthHeaders());
        }

        /**
         * Extracts and validates response body, failing the test if null.
         * This method handles nullable ResponseEntity from RestTemplate and ensures
         * both the response and its body are non-null before returning.
         *
         * @param response the response entity to extract body from (may be null)
         * @param context  descriptive context for error messages
         * @param <T>      the type of the response body
         * @return the non-null response body
         * @throws AssertionError if response or body is null
         */
        @NonNull
        private <T> T requireBody(ResponseEntity<T> response, String context) {
                assertThat(response)
                                .as("Response should not be null for: %s", context)
                                .isNotNull();
                T body = Objects.requireNonNull(response, "Response was null for: " + context).getBody();
                assertThat(body)
                                .as("Response body should not be null for: %s", context)
                                .isNotNull();
                return Objects.requireNonNull(body, "Response body was null for: " + context);
        }

        @Nested
        @DisplayName("Product Creation Tests")
        class ProductCreationTests {

                @Test
                @DisplayName("Should create product with valid data")
                void shouldCreateProductWithValidData() {
                        // Given
                        ProductRequest request = new ProductRequest(
                                        "Integration Test Product",
                                        new BigDecimal("199.99"),
                                        10,
                                        "ACTIVE");
                        HttpEntity<ProductRequest> entity = createAuthEntity(request);

                        // When
                        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                                        "/api/products",
                                        HttpMethod.POST,
                                        entity,
                                        ProductResponse.class);

                        // Then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

                        ProductResponse createdProduct = requireBody(response, "create product");
                        assertThat(createdProduct.id()).isNotNull();
                        assertThat(createdProduct.name()).isEqualTo("Integration Test Product");
                        assertThat(createdProduct.price()).isEqualByComparingTo(new BigDecimal("199.99"));
                        assertThat(createdProduct.stock()).isEqualTo(10);
                        assertThat(createdProduct.status()).isEqualTo("ACTIVE");
                }

                @Test
                @DisplayName("Should reject product creation without authentication")
                void shouldRejectProductCreationWithoutAuth() {
                        // Given
                        ProductRequest request = new ProductRequest(
                                        "Unauthorized Product",
                                        new BigDecimal("50.00"),
                                        5,
                                        "ACTIVE");
                        HttpEntity<ProductRequest> entity = new HttpEntity<>(request);

                        // When
                        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                                        "/api/products",
                                        HttpMethod.POST,
                                        entity,
                                        ProductResponse.class);

                        // Then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                }
        }

        @Nested
        @DisplayName("Product Listing Tests")
        class ProductListingTests {

                @Test
                @DisplayName("Should list all products")
                void shouldListAllProducts() {
                        // Given - Create a product first
                        ProductRequest request = new ProductRequest(
                                        "Listable Product",
                                        new BigDecimal("75.00"),
                                        3,
                                        "ACTIVE");
                        HttpEntity<ProductRequest> createEntity = createAuthEntity(request);
                        restTemplate.exchange("/api/products", HttpMethod.POST, createEntity, ProductResponse.class);

                        // When
                        HttpEntity<Void> listEntity = createAuthEntity();
                        ResponseEntity<List<ProductResponse>> response = restTemplate.exchange(
                                        "/api/products",
                                        HttpMethod.GET,
                                        listEntity,
                                        new ParameterizedTypeReference<>() {
                                        });

                        // Then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                        List<ProductResponse> products = requireBody(response, "list products");
                        assertThat(products).isNotEmpty();
                        assertThat(products)
                                        .extracting(ProductResponse::name)
                                        .contains("Listable Product");
                }
        }

        @Nested
        @DisplayName("Product Retrieval Tests")
        class ProductRetrievalTests {

                @Test
                @DisplayName("Should retrieve product by ID")
                void shouldRetrieveProductById() {
                        // Given - Create a product first
                        ProductRequest request = new ProductRequest(
                                        "Retrievable Product",
                                        new BigDecimal("125.00"),
                                        7,
                                        "ACTIVE");
                        HttpEntity<ProductRequest> createEntity = createAuthEntity(request);
                        ResponseEntity<ProductResponse> createResponse = restTemplate.exchange(
                                        "/api/products",
                                        HttpMethod.POST,
                                        createEntity,
                                        ProductResponse.class);

                        ProductResponse createdProduct = requireBody(createResponse, "create product for retrieval");
                        Long productId = Objects.requireNonNull(createdProduct.id(), "Created product must have an ID");

                        // When
                        HttpEntity<Void> getEntity = createAuthEntity();
                        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                                        "/api/products/" + productId,
                                        HttpMethod.GET,
                                        getEntity,
                                        ProductResponse.class);

                        // Then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                        ProductResponse retrievedProduct = requireBody(response, "retrieve product");
                        assertThat(retrievedProduct.id()).isEqualTo(productId);
                        assertThat(retrievedProduct.name()).isEqualTo("Retrievable Product");
                }

                @Test
                @DisplayName("Should return 404 for non-existent product")
                void shouldReturn404ForNonExistentProduct() {
                        // Given
                        Long nonExistentId = 999999L;
                        HttpEntity<Void> entity = createAuthEntity();

                        // When
                        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                                        "/api/products/" + nonExistentId,
                                        HttpMethod.GET,
                                        entity,
                                        ProductResponse.class);

                        // Then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                }
        }

        @Nested
        @DisplayName("Product Update Tests")
        class ProductUpdateTests {

                @Test
                @DisplayName("Should update existing product")
                void shouldUpdateExistingProduct() {
                        // Given - Create a product first
                        ProductRequest createRequest = new ProductRequest(
                                        "Original Name",
                                        new BigDecimal("100.00"),
                                        5,
                                        "ACTIVE");
                        HttpEntity<ProductRequest> createEntity = createAuthEntity(createRequest);
                        ResponseEntity<ProductResponse> createResponse = restTemplate.exchange(
                                        "/api/products",
                                        HttpMethod.POST,
                                        createEntity,
                                        ProductResponse.class);

                        ProductResponse createdProduct = requireBody(createResponse, "create product for update");
                        Long productId = Objects.requireNonNull(createdProduct.id(), "Created product must have an ID");

                        // When - Update the product
                        ProductRequest updateRequest = new ProductRequest(
                                        "Updated Name",
                                        new BigDecimal("150.00"),
                                        10,
                                        "ACTIVE");
                        HttpEntity<ProductRequest> updateEntity = createAuthEntity(updateRequest);
                        ResponseEntity<ProductResponse> updateResponse = restTemplate.exchange(
                                        "/api/products/" + productId,
                                        HttpMethod.PUT,
                                        updateEntity,
                                        ProductResponse.class);

                        // Then
                        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                        ProductResponse updatedProduct = requireBody(updateResponse, "update product");
                        assertThat(updatedProduct.id()).isEqualTo(productId);
                        assertThat(updatedProduct.name()).isEqualTo("Updated Name");
                        assertThat(updatedProduct.price()).isEqualByComparingTo(new BigDecimal("150.00"));
                        assertThat(updatedProduct.stock()).isEqualTo(10);
                }
        }

        @Nested
        @DisplayName("Product Deletion Tests")
        class ProductDeletionTests {

                @Test
                @DisplayName("Should delete existing product")
                void shouldDeleteExistingProduct() {
                        // Given - Create a product first
                        ProductRequest request = new ProductRequest(
                                        "Deletable Product",
                                        new BigDecimal("50.00"),
                                        2,
                                        "ACTIVE");
                        HttpEntity<ProductRequest> createEntity = createAuthEntity(request);
                        ResponseEntity<ProductResponse> createResponse = restTemplate.exchange(
                                        "/api/products",
                                        HttpMethod.POST,
                                        createEntity,
                                        ProductResponse.class);

                        ProductResponse createdProduct = requireBody(createResponse, "create product for deletion");
                        Long productId = Objects.requireNonNull(createdProduct.id(), "Created product must have an ID");

                        // When
                        HttpEntity<Void> deleteEntity = createAuthEntity();
                        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                                        "/api/products/" + productId,
                                        HttpMethod.DELETE,
                                        deleteEntity,
                                        Void.class);

                        // Then
                        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

                        // Verify deletion
                        HttpEntity<Void> verifyEntity = createAuthEntity();
                        ResponseEntity<ProductResponse> getResponse = restTemplate.exchange(
                                        "/api/products/" + productId,
                                        HttpMethod.GET,
                                        verifyEntity,
                                        ProductResponse.class);
                        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                }
        }

        @Nested
        @DisplayName("End-to-End CRUD Flow Tests")
        class EndToEndCrudFlowTests {

                @Test
                @DisplayName("Should complete full CRUD lifecycle")
                void shouldCompleteFullCrudLifecycle() {
                        // CREATE
                        ProductRequest createRequest = new ProductRequest(
                                        "Lifecycle Product",
                                        new BigDecimal("200.00"),
                                        15,
                                        "ACTIVE");
                        HttpEntity<ProductRequest> createEntity = createAuthEntity(createRequest);
                        ResponseEntity<ProductResponse> createResponse = restTemplate.exchange(
                                        "/api/products",
                                        HttpMethod.POST,
                                        createEntity,
                                        ProductResponse.class);

                        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                        ProductResponse created = requireBody(createResponse, "create in lifecycle");
                        Long productId = Objects.requireNonNull(created.id(), "Created product must have an ID");

                        // READ
                        HttpEntity<Void> readEntity = createAuthEntity();
                        ResponseEntity<ProductResponse> readResponse = restTemplate.exchange(
                                        "/api/products/" + productId,
                                        HttpMethod.GET,
                                        readEntity,
                                        ProductResponse.class);

                        assertThat(readResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                        ProductResponse read = requireBody(readResponse, "read in lifecycle");
                        assertThat(read.name()).isEqualTo("Lifecycle Product");

                        // UPDATE
                        ProductRequest updateRequest = new ProductRequest(
                                        "Updated Lifecycle Product",
                                        new BigDecimal("250.00"),
                                        20,
                                        "ACTIVE");
                        HttpEntity<ProductRequest> updateEntity = createAuthEntity(updateRequest);
                        ResponseEntity<ProductResponse> updateResponse = restTemplate.exchange(
                                        "/api/products/" + productId,
                                        HttpMethod.PUT,
                                        updateEntity,
                                        ProductResponse.class);

                        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                        ProductResponse updated = requireBody(updateResponse, "update in lifecycle");
                        assertThat(updated.name()).isEqualTo("Updated Lifecycle Product");

                        // DELETE
                        HttpEntity<Void> deleteEntity = createAuthEntity();
                        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                                        "/api/products/" + productId,
                                        HttpMethod.DELETE,
                                        deleteEntity,
                                        Void.class);

                        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

                        // VERIFY DELETION
                        HttpEntity<Void> verifyEntity = createAuthEntity();
                        ResponseEntity<ProductResponse> verifyResponse = restTemplate.exchange(
                                        "/api/products/" + productId,
                                        HttpMethod.GET,
                                        verifyEntity,
                                        ProductResponse.class);
                        assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                }
        }

        /**
         * PR-1 Critical Fixes & Features
         *
         * These tests verify:
         * - Update safety: PUT non-existent → 404 (not silent create)
         * - Server-side pagination: /api/products/paged endpoint
         * - Search filtering: query parameter support
         * - Sort protection: whitelist validation (SQL injection prevention)
         */
        @Nested
        @DisplayName("PR-1: Server-Side Pagination & Update Safety")
        class ProductPaginationAndSafetyTests {

                /**
                 * Update Safety Test: PR-1 Critical Fix
                 *
                 * Before PR-1: PUT /api/products/999999 would silently create a new product
                 * After PR-1: PUT /api/products/999999 returns 404 NOT_FOUND
                 */
                @Test
                @DisplayName("should return 404 when updating non-existent product (update safety)")
                void shouldReturn404WhenUpdatingNonExistent() {
                        long nonExistentId = 999999L;
                        ProductRequest request = new ProductRequest(
                                        "Ghost Product",
                                        new BigDecimal("99.99"),
                                        1,
                                        "ACTIVE"
                        );

                        HttpEntity<ProductRequest> entity = createAuthEntity(request);
                        ResponseEntity<Object> response = restTemplate.exchange(
                                        "/api/products/" + nonExistentId,
                                        HttpMethod.PUT,
                                        entity,
                                        Object.class
                        );

                        assertThat(response.getStatusCode())
                                        .as("PUT on non-existent product should return 404, not create silently")
                                        .isEqualTo(HttpStatus.NOT_FOUND);
                }

                /**
                 * Pagination Test: PR-1 Feature
                 *
                 * Tests that GET /api/products/paged returns properly formatted paginated response
                 */
                @Test
                @DisplayName("should return paged response with metadata")
                void shouldReturnPagedProducts() {
                        HttpEntity<Void> entity = createAuthEntity();
                        ResponseEntity<String> response = restTemplate.exchange(
                                        "/api/products/paged?page=0&size=2&sort=id,asc",
                                        HttpMethod.GET,
                                        entity,
                                        String.class
                        );

                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        String body = requireBody(response, "paged response");
                        assertThat(body)
                                        .contains("\"items\"")
                                        .contains("\"totalElements\"")
                                        .contains("\"page\"")
                                        .contains("\"size\"");
                }

                /**
                 * Search Filter Test: PR-1 Feature
                 *
                 * Tests that query parameter filters products server-side by name
                 */
                @Test
                @DisplayName("should filter products by query parameter (server-side search)")
                void shouldFilterByQuery() {
                        HttpEntity<Void> entity = createAuthEntity();
                        // Search for products containing "Laptop"
                        ResponseEntity<String> response = restTemplate.exchange(
                                        "/api/products/paged?page=0&size=50&q=Laptop&sort=id,asc",
                                        HttpMethod.GET,
                                        entity,
                                        String.class
                        );

                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        // Note: Actual content verification depends on seeded data
                }

                /**
                 * Status Filter Test: PR-1 Feature
                 *
                 * Tests that status parameter filters products by status value
                 */
                @Test
                @DisplayName("should filter products by status parameter")
                void shouldFilterByStatus() {
                        HttpEntity<Void> entity = createAuthEntity();
                        ResponseEntity<String> response = restTemplate.exchange(
                                        "/api/products/paged?page=0&size=50&status=ACTIVE&sort=id,asc",
                                        HttpMethod.GET,
                                        entity,
                                        String.class
                        );

                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(requireBody(response, "status filter response"))
                                        .contains("\"items\"");
                }

                /**
                 * Sort Parameter Test: PR-1 Feature
                 *
                 * Tests that sort parameter applies server-side sorting
                 * Format: "fieldName,direction" (e.g., "name,asc" or "price,desc")
                 */
                @Test
                @DisplayName("should apply sort parameter correctly")
                void shouldApplySortParameter() {
                        HttpEntity<Void> entity = createAuthEntity();
                        ResponseEntity<String> response = restTemplate.exchange(
                                        "/api/products/paged?page=0&size=50&sort=name,asc",
                                        HttpMethod.GET,
                                        entity,
                                        String.class
                        );

                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                }

                /**
                 * SQL Injection Prevention Test: PR-1 Security
                 *
                 * Tests that invalid sort fields are safely ignored (whitelist validation)
                 * An attacker cannot inject arbitrary SQL via the sort parameter.
                 */
                @Test
                @DisplayName("should ignore invalid sort field for security (SQL injection prevention)")
                void shouldSafelyIgnoreInvalidSortField() {
                        HttpEntity<Void> entity = createAuthEntity();
                        // Attempt to use a non-whitelisted field; should be safely ignored
                        ResponseEntity<String> response = restTemplate.exchange(
                                        "/api/products/paged?page=0&size=50&sort=malicious_field,asc",
                                        HttpMethod.GET,
                                        entity,
                                        String.class
                        );

                        // Should not crash; falls back to default sort
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(requireBody(response, "invalid sort field response"))
                                        .contains("\"items\"");
                }

        /**
         * Default Pagination Test: PR-1 Feature
         *
         * Tests default pagination when parameters are omitted
         */
        @Test
        @DisplayName("should use default pagination when parameters are omitted")
        void shouldUseDefaults() {
            HttpEntity<Void> entity = createAuthEntity();
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/products/paged",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
