# 🛒 Catalog App - Portfolio Project

![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)


> Full-stack product catalog with Spring Boot 3.4 + Java 21 + PostgreSQL

## 🎯 Project Overview

## 🎯 Project Overview

This project was developed to showcase the core pillars of a modern product catalog system: security, architecture, and data management. Beyond being just a CRUD application, it is an example of "Clean Architecture" that prioritizes architectural flexibility and security standards.

### Key Features
- **Clean Architecture**: Independent business logic and easy testability.
- **Domain-Driven Design (Lite)**: Focused layering and domain isolation.
- **Dual Authentication**: Simultaneous Session and JWT-based authorization.
- **Role-Based Access Control (RBAC)**: Secure access management with Admin and User roles.
- **Database Versioning**: Controlled schema management using Flyway.

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

## 📖 Key Learnings
During this intense 4-day sprint, the following architectural experiences were gained beyond just coding:
- **Hybrid Security**: How to harmoniously integrate traditional web applications and modern API services under the same security umbrella (Spring Security 6).
- **Speed vs. Quality Balance**: How to maintain architectural integrity while meeting tight deadlines (Pragmatic DDD approach).
- **Infrastructure Automation**: Establishing a seamless local-to-production flow using Docker Compose and GitHub Actions.
- **Contract-First Development**: Standardizing communication between frontend and backend using OpenAPI (Swagger).

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

This project was designed with the goal of building a sustainable structure that meets modern standards within a limited timeframe (4 days).

### 🏠 Why Clean Architecture?
As a portfolio project, it was primary to demonstrate that the business logic can remain independent of frameworks (Spring Boot, etc.).
- **Domain Layer**: Business rules are centralized here, isolated from the outside world (DB, Web).
- **Port-Adapter Pattern**: Ensured that the business logic remains unaffected even if database or UI technologies change.

### 🔐 Dual Security Strategy
One of the major architectural choices was to use both **Session-based (Form Login)** and **Stateless (JWT)** structures simultaneously in the same application.
- **Web UI**: Session management was preferred for a fast, SEO-friendly, and secure (CSRF protected) interface using Thymeleaf.
- **API**: A stateless JWT infrastructure was established to be ready for future Angular or mobile application integrations.
- **Trade-off**: While managing two different `SecurityFilterChain`s increases complexity, this cost was accepted for maximum flexibility.

### ⏱️ Time Management and Sprint Strategy
To deliver a high-quality, working product within 4 days, the following methods were applied:
- **Pragmatic DDD**: Instead of complex Value Object structures, JPA Entities and simple DTOs were used in the first phase (DDD-lite).
- **Manual Mapping**: Manual mapping was preferred for transparency and ease of debugging, rather than spending time on MapStruct configurations.
- **SSR-First**: To meet the MVP target, a Thymeleaf + Bootstrap interface was prioritized over spending time on a full Angular SPA in the first phase.

### ⚖️ Trade-offs
1. **Database Access**: Instead of completely decoupling domain models from Entities, JPA Entities were kept close to the domain layer to increase development speed.
2. **Validation**: Validation logic was maintained in both DTOs (Jakarta Validation) and the domain level to ensure a "fail-fast" approach, which slightly increases code duplication but maximizes security.
3. **Frontend**: A classic Web App structure was built instead of a rich SPA; however, APIs were left completely decoupled to keep the migration path open.
