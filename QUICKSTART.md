# 🚀 Quick Start Guide - Day 1

## ✅ What We Built Today

### Architecture Foundation
- ✅ Clean Architecture package structure
- ✅ Dual SecurityFilterChain (Web + API)
- ✅ JWT authentication infrastructure
- ✅ PostgreSQL with Flyway migrations
- ✅ REST API with Swagger UI
- ✅ Data seeding with BCrypt passwords

### Key Components
1. **Security Layer**: Two separate chains
   - Web UI: Form login + session (CSRF enabled)
   - API: JWT stateless (CSRF disabled)

2. **Persistence**: 
   - UserEntity + ProductEntity
   - Spring Data JPA repositories
   - Flyway V1 migration

3. **API Endpoints**:
   - POST `/api/auth/login` - Get JWT token
   - GET `/api/products` - List products (authenticated)
   - POST `/api/products` - Create product (admin)
   - PUT `/api/products/{id}` - Update product (admin)
   - DELETE `/api/products/{id}` - Delete product (admin)

---

## 🏃 Run the Application

### Step 1: Start PostgreSQL
```bash
cd backend
docker compose up -d
```

### Step 2: Verify Database
```bash
docker compose ps
# Should show postgres running on port 5432
```

### Step 3: Run Backend
```bash
./gradlew bootRun
```

**Expected Output:**
```
Started CatalogApplication in X.XXX seconds
✓ Users seeded successfully
✓ Products seeded successfully
```

### Step 4: Test Endpoints

#### Access Swagger UI
Open: http://localhost:8080/swagger-ui.html

#### Login via API
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "ROLE_ADMIN"
}
```

#### List Products with Token
```bash
# Replace <TOKEN> with actual token from login
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer <TOKEN>"
```

#### Access Login Page
Open: http://localhost:8080/login
- Username: `admin`
- Password: `admin123`

---

## 📋 Definition of Done - Day 1 ✅

- [x] Docker Compose PostgreSQL running
- [x] Flyway migrations applied
- [x] Admin + User seeded with BCrypt passwords
- [x] JWT authentication working
- [x] Dual SecurityFilterChain configured
- [x] REST API CRUD endpoints functional
- [x] Role-based authorization (admin vs user)
- [x] Swagger UI accessible
- [x] Login page rendering
- [x] Health check: http://localhost:8080/actuator/health

---

## 🧪 Manual Test Checklist

### API Tests (via Swagger UI or curl)

1. **Login as Admin**
   ```
   POST /api/auth/login
   Body: {"username":"admin","password":"admin123"}
   Expected: 200 + token
   ```

2. **Login as User**
   ```
   POST /api/auth/login
   Body: {"username":"user","password":"user123"}
   Expected: 200 + token
   ```

3. **List Products (Authenticated)**
   ```
   GET /api/products
   Header: Authorization: Bearer <token>
   Expected: 200 + list of 5 products
   ```

4. **Create Product (Admin)**
   ```
   POST /api/products
   Header: Authorization: Bearer <admin_token>
   Body: {"name":"Test","price":99.99,"stock":10,"status":"ACTIVE"}
   Expected: 201 + created product
   ```

5. **Create Product (User - Should Fail)**
   ```
   POST /api/products
   Header: Authorization: Bearer <user_token>
   Expected: 403 Forbidden
   ```

6. **Update Product (Admin)**
   ```
   PUT /api/products/1
   Header: Authorization: Bearer <admin_token>
   Body: {"name":"Updated","price":150,"stock":20,"status":"ACTIVE"}
   Expected: 200 + updated product
   ```

7. **Delete Product (Admin)**
   ```
   DELETE /api/products/5
   Header: Authorization: Bearer <admin_token>
   Expected: 204 No Content
   ```

### Web UI Tests

1. **Login Page**
   - Navigate to: http://localhost:8080/login
   - Expected: Bootstrap styled login form
   - Demo credentials visible

2. **Login Success**
   - Login with: admin / admin123
   - Expected: Redirect to /products (currently 404 - will fix Day 2)

3. **Login Failure**
   - Login with: invalid / wrong
   - Expected: Error message "Invalid username or password"

---

## 🗂️ Project Structure

```
catalog-app/
├── README.md
├── .gitignore
└── backend/
    ├── build.gradle.kts
    ├── settings.gradle.kts
    ├── docker-compose.yml
    ├── .env.example
    ├── api-test.http
    ├── gradlew
    ├── gradlew.bat
    ├── gradle/wrapper/
    └── src/
        ├── main/
        │   ├── java/com/daghan/catalog/
        │   │   ├── CatalogApplication.java
        │   │   ├── application/
        │   │   │   └── dto/
        │   │   ├── domain/              # (Empty - Day 2)
        │   │   ├── infrastructure/
        │   │   │   ├── config/
        │   │   │   ├── persistence/
        │   │   │   │   ├── entity/
        │   │   │   │   ├── repository/
        │   │   │   │   └── DataSeeder.java
        │   │   │   └── security/
        │   │   └── interfaces/
        │   │       └── web/
        │   │           ├── rest/
        │   │           └── mvc/
        │   └── resources/
        │       ├── application.yml
        │       ├── db/migration/
        │       │   └── V1__init_schema.sql
        │       └── templates/
        │           └── login.html
        └── test/
            └── java/com/daghan/catalog/
                └── CatalogApplicationTests.java
```

---

## 🛠️ Troubleshooting

### Port 5432 Already in Use
```bash
# Check what's using the port
lsof -i :5432

# Stop existing PostgreSQL
# macOS: brew services stop postgresql
# Linux: sudo systemctl stop postgresql
```

### Flyway Migration Fails
```bash
# Clean and restart
docker compose down -v
docker compose up -d
./gradlew clean bootRun
```

### JWT Token Issues
- Check `application.yml` - `app.jwt.secret` must be 32+ characters
- Verify token is passed in header: `Authorization: Bearer <token>`

---

## 📝 Git Commit Strategy

```bash
git add .
git commit -m "chore: bootstrap project with postgres and flyway"
git commit -m "feat(persistence): add user/product entities and repositories"
git commit -m "feat(security): add dual filter chain (web formLogin, api jwt)"
git commit -m "feat(api): add auth login and product crud endpoints"
git commit -m "feat(ui): add thymeleaf login page"
git commit -m "docs: add quickstart guide"
```

---

## 🎯 Tomorrow (Day 2) - Preview

### Goals
- [ ] Products list page (Thymeleaf)
- [ ] Admin CRUD UI (forms/modals)
- [ ] Global exception handler (ProblemDetail)
- [ ] Validation error responses
- [ ] Use-case layer (clean architecture)

### Time Estimate: 10 hours
- 09:00-12:00: Exception handling + validation
- 13:00-16:00: Thymeleaf products page
- 16:00-19:00: Admin CRUD UI

---

## 💡 Key Learnings

### Security Architecture
- Two `SecurityFilterChain` beans with `@Order` prioritization
- API chain matches `/api/**` first (stateless JWT)
- Web chain catches everything else (session-based)

### JWT Flow
1. Client sends credentials to `/api/auth/login`
2. Server validates via `AuthenticationManager`
3. Server generates JWT with username + role claims
4. Client stores token
5. Client sends token in `Authorization: Bearer <token>` header
6. `JwtAuthenticationFilter` validates and sets `SecurityContext`

### Flyway Best Practice
- Schema in SQL (V1__init_schema.sql)
- Data seed in Java (CommandLineRunner)
- Reason: BCrypt password hashing requires runtime

---

**Status**: ✅ Day 1 Complete - Foundation Solid  
**Next**: Day 2 - UI + Exception Handling  
**Deadline**: On Track 🎯
