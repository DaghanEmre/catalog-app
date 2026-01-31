package com.daghan.catalog.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Catalog App API",
                version = "1.0.0",
                description = """
                        Product Catalog Management API
                        
                        This API provides endpoints for managing products with role-based access control.
                        
                        **Authentication:**
                        1. Login via `/api/auth/login` to get a JWT token
                        2. Use the token in the Authorization header: `Bearer <token>`
                        
                        **Roles:**
                        - ADMIN: Full CRUD access to products
                        - USER: Read-only access to products
                        
                        **Demo Credentials:**
                        - Admin: admin / admin123
                        - User: user / user123
                        """,
                contact = @Contact(
                        name = "Daghan Emre",
                        url = "https://github.com/DaghanEmre"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development Server")
        }
)
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT token obtained from /api/auth/login endpoint"
)
public class OpenApiConfig {
}
