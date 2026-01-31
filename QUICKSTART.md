# ⚡ Quick Start Guide

Get the project running on your local machine in under 5 minutes.

---

## 1. Prerequisites
- **Java**: JDK 21+
- **Docker**: Desktop or Engine + Compose
- **Shell**: Bash/Zsh (Linux/macOS)

---

## 2. Infrastructure Setup
The project uses Docker for its dependency layer (PostgreSQL).

```bash
cd backend
docker compose up -d
```

---

## 3. Environment Configuration
Copy the template and adjust if necessary (default values work for local dev).

```bash
cp .env.example .env
```

---

## 4. Run Application
Use the Gradle wrapper to start the Spring Boot context.

```bash
./gradlew bootRun
```

---

## 5. Verify Installation

- **Web UI**: [http://localhost:8080](http://localhost:8080)
- **API Health**: `curl http://localhost:8080/actuator/health`

### Default Accounts
| Role | Username | Password |
| :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` |
| **User** | `user` | `user123` |

---

## 🧪 Running Tests
```bash
./gradlew test
```
