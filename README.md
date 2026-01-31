# 🛒 Catalog App - Portfolio Project

![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)


> Full-stack product catalog with Spring Boot 3.4 + Java 21 + PostgreSQL

## 🎯 Project Overview

Secure product management system demonstrating:
- Clean Architecture principles
- Domain-Driven Design (DDD-lite)
- RESTful API design
- Spring Security + JWT authentication
- Role-based access control (RBAC)

## 🏗️ Architecture

### Tech Stack
- **Backend**: Java 21, Spring Boot 3.4.1, Hibernate, Flyway
- **Database**: PostgreSQL 16
- **Security**: Spring Security 6, JWT (API) + Form Login (Web UI)
- **Frontend**: Thymeleaf + Bootstrap 5 (Phase 1) / Angular + PrimeNG (Phase 2)
- **Build**: Gradle 8.5

### Package Structure
```
com.daghan.catalog/
├── domain/          # Business logic & entities
├── application/     # Use cases & DTOs
├── infrastructure/  # JPA, Security, External services
└── interfaces/      # REST controllers, MVC controllers
```

## 🚀 Quick Start

### Prerequisites
- Java 21
- Docker & Docker Compose

### Run Application
```bash
# Start PostgreSQL
cd backend
docker compose up -d

# Run backend
./gradlew bootRun

# Access application
open http://localhost:8080
```

### Default Credentials
- **Admin**: `admin` / `admin123`
- **User**: `user` / `user123`

## 📚 API Documentation

Once running, visit:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

### Authentication
```bash
# Login (API)
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response: { "token": "eyJhbG...", "username": "admin", "role": "ROLE_ADMIN" }
```

### Products (Admin Only)
```bash
# Create Product
POST /api/products
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Laptop",
  "price": 1299.99,
  "stock": 10,
  "status": "ACTIVE"
}

# Update Product
PUT /api/products/{id}
Authorization: Bearer <token>

# Delete Product
DELETE /api/products/{id}
Authorization: Bearer <token>

# List Products (Authenticated users)
GET /api/products
Authorization: Bearer <token>
```

## 🧪 Testing
```bash
./gradlew test
```

## 📝 Development Log

### Sprint Plan (30 Jan - 3 Feb 2026)

**Day 1** (Jan 30): Foundation
- [x] Project skeleton
- [x] PostgreSQL + Docker Compose
- [x] Flyway migrations
- [x] Dual SecurityFilterChain (Web + API)
- [x] JWT infrastructure
- [x] REST API basics

**Day 2** (Jan 31): Security + CRUD
- [ ] Form login (Web UI)
- [ ] Full CRUD endpoints
- [ ] Global exception handling
- [ ] Swagger UI integration

**Day 3** (Feb 1): Frontend
- [ ] Thymeleaf product pages
- [ ] Admin CRUD interface
- [ ] Bootstrap styling

**Day 4** (Feb 2): Polish + Documentation
- [ ] Unit tests
- [ ] Observability (Actuator)
- [ ] README finalization
- [ ] Architecture diagram

## 🔮 Future Enhancements
- [ ] Angular + PrimeNG frontend
- [ ] Testcontainers integration tests
- [ ] Observability (Prometheus/Grafana)
- [ ] Redis caching layer
- [ ] Pagination & filtering
- [ ] Product images upload

## 📖 What I Learned
- Implementing dual authentication strategies (session + JWT)
- Clean separation of domain and infrastructure concerns
- Flyway migrations for database versioning
- Role-based authorization in Spring Security

## 👨‍💻 Author
**Daghan Emre**
- GitHub: [@DaghanEmre](https://github.com/DaghanEmre)
- Project: Portfolio demonstration piece
- Timeline: 4-day sprint (Jan 30 - Feb 3, 2026)

## 📄 License
This project is licensed under the **GNU GPL v3.0** - see the [LICENSE](LICENSE) file for details.

---
## 🚀 Deployment (CI/CD)
The project includes a GitHub Actions workflow for automatic deployment via SSH.

### Required Secrets
To use the `deploy.yml` workflow, add the following secrets to your GitHub repository:
- `SERVER_HOST`: Your server IP or domain.
- `SERVER_USER`: SSH username.
- `SSH_PRIVATE_KEY`: Your SSH private key.
- `SERVER_PORT`: SSH port (default 22).


---

## 🏛️ Architecture Decisions

### Why Two Authentication Mechanisms?
- **Web UI (Session)**: Fast development, CSRF protection, traditional approach
- **API (JWT)**: Stateless, scalable, SPA-ready for future Angular migration

### Why Clean Architecture?
- Framework independence in domain layer
- Testability
- Separation of concerns
- Portfolio demonstrates architectural thinking

### Trade-offs Made
- **Domain vs Entity**: Pragmatic approach - JPA entities initially, domain model extracted when needed
- **Thymeleaf over Angular initially**: Delivery speed prioritized, SPA migration path preserved via RESTful API
- **Manual mapping over MapStruct**: Simplicity and clarity for portfolio review
