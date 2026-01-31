# 📋 Git Setup & Initial Commit Guide

## Step 1: GitHub Repository Setup

### On GitHub (github.com)
1. Navigate to: https://github.com/DaghanEmre
2. Click "New Repository"
3. Repository details:
   - **Name**: `catalog-app-java21-spring`
   - **Description**: `Full-stack product catalog with Spring Boot 3.4 + Java 21 + PostgreSQL`
   - **Visibility**: Public
   - **Initialize**: ❌ Do NOT add README, .gitignore, or license
4. Click "Create repository"

---

## Step 2: Local Git Initialization

```bash
cd /path/to/catalog-app

# Initialize Git
git init

# Add remote
git remote add origin https://github.com/DaghanEmre/catalog-app-java21-spring.git

# Check current branch
git branch -M main
```

---

## Step 3: Initial Commit (Atomic Commits)

### Commit 1: Project Bootstrap
```bash
git add .gitignore .gitattributes README.md QUICKSTART.md
git add backend/build.gradle.kts backend/settings.gradle.kts
git add backend/gradle/
git add backend/gradlew backend/gradlew.bat
git add backend/docker-compose.yml backend/.env.example
git commit -m "chore: bootstrap project with gradle and docker"
```

### Commit 2: Database Schema
```bash
git add backend/src/main/resources/db/migration/
git add backend/src/main/resources/application.yml
git commit -m "feat(db): add flyway migrations and configuration"
```

### Commit 3: Persistence Layer
```bash
git add backend/src/main/java/com/daghan/catalog/infrastructure/persistence/entity/
git add backend/src/main/java/com/daghan/catalog/infrastructure/persistence/repository/
git add backend/src/main/java/com/daghan/catalog/infrastructure/persistence/DataSeeder.java
git commit -m "feat(persistence): add user/product entities and repositories"
```

### Commit 4: Security Infrastructure
```bash
git add backend/src/main/java/com/daghan/catalog/infrastructure/security/
git commit -m "feat(security): add dual filter chain with JWT and form login"
```

### Commit 5: Application Layer
```bash
git add backend/src/main/java/com/daghan/catalog/CatalogApplication.java
git add backend/src/main/java/com/daghan/catalog/application/dto/
git add backend/src/main/java/com/daghan/catalog/infrastructure/config/
git commit -m "feat(app): add DTOs and OpenAPI configuration"
```

### Commit 6: REST API
```bash
git add backend/src/main/java/com/daghan/catalog/interfaces/web/rest/
git add backend/api-test.http
git commit -m "feat(api): add authentication and product REST endpoints"
```

### Commit 7: Web UI
```bash
git add backend/src/main/java/com/daghan/catalog/interfaces/web/mvc/
git add backend/src/main/resources/templates/
git commit -m "feat(ui): add thymeleaf login page"
```

### Commit 8: Tests
```bash
git add backend/src/test/
git commit -m "test: add basic application context test"
```

---

## Step 4: Push to GitHub

```bash
# Push to main branch
git push -u origin main

# Verify on GitHub
# Visit: https://github.com/DaghanEmre/catalog-app-java21-spring
```

---

## Alternative: Single Initial Commit

If you prefer a single initial commit:

```bash
git add .
git commit -m "feat: initial project setup with Spring Boot 3.4, JWT auth, and dual security chains

- Bootstrap Spring Boot 3.4.1 + Java 21 project
- Add PostgreSQL with Docker Compose
- Implement dual SecurityFilterChain (web + API)
- Add JWT authentication for REST API
- Add form login for web UI
- Create user and product entities with repositories
- Add Flyway migrations for database schema
- Implement CRUD REST endpoints for products
- Add Swagger UI for API documentation
- Create Thymeleaf login page
- Seed initial admin and user accounts"

git push -u origin main
```

---

## Git Configuration (First Time Setup)

```bash
# Set your identity
git config --global user.name "Daghan Emre"
git config --global user.email "your.email@example.com"

# Optional: Set default branch to main
git config --global init.defaultBranch main

# Optional: Set editor
git config --global core.editor "code --wait"  # VS Code
# or
git config --global core.editor "vim"          # Vim
```

---

## Verify Setup

```bash
# Check remote
git remote -v

# Check status
git status

# Check commit history
git log --oneline

# Check branch
git branch -a
```

---

## GitHub Best Practices

### Branch Protection (Optional - Setup Later)
1. Settings → Branches → Add rule
2. Branch name pattern: `main`
3. Enable:
   - Require pull request before merging
   - Require status checks to pass

### Repository Topics (Add on GitHub)
- `spring-boot`
- `java-21`
- `postgresql`
- `jwt`
- `rest-api`
- `clean-architecture`
- `portfolio-project`

### Repository Description
```
Full-stack product catalog demonstrating Clean Architecture, 
Spring Security with dual authentication (JWT + Form Login), 
and RESTful API design. Built with Spring Boot 3.4 and Java 21.
```

---

## Next Steps After Push

1. ✅ Verify repository is visible on GitHub
2. ✅ Check all files are present
3. ✅ README renders correctly
4. ✅ Add repository topics
5. ✅ Star your own repository (portfolio boost! 😄)
6. 🚀 Continue with Day 2 development

---

## Troubleshooting

### Authentication Issues

**HTTPS (Recommended for Portfolio)**
```bash
# GitHub will prompt for username + Personal Access Token
git push -u origin main
```

**Generate Personal Access Token:**
1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token
3. Scopes: Select `repo` (all)
4. Copy token and use as password

**SSH (Alternative)**
```bash
# Change remote to SSH
git remote set-url origin git@github.com:DaghanEmre/catalog-app-java21-spring.git

# Add SSH key if not done
ssh-keygen -t ed25519 -C "your.email@example.com"
# Add to GitHub: Settings → SSH and GPG keys
```

### Push Rejected
```bash
# If GitHub repo has README/LICENSE you created
git pull origin main --allow-unrelated-histories
git push -u origin main
```

---

**Ready to push!** 🚀
