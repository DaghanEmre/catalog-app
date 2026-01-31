# 🛒 Catalog App - Portfolio Project

![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)


> Full-stack product catalog with Spring Boot 3.4 + Java 21 + PostgreSQL

## 🎯 Project Overview

Bu proje, modern bir ürün katalog sisteminin temel taşlarını (güvenlik, mimari, veri yönetimi) sergilemek amacıyla geliştirilmiştir. Sadece bir CRUD uygulaması olmanın ötesinde, mimari esneklik ve güvenlik standartlarını ön planda tutan bir "Clean Architecture" örneğidir.

### Temel Özellikler
- **Clean Architecture**: Bağımsız iş mantığı ve kolay test edilebilirlik.
- **Domain-Driven Design (Lite)**: İş odaklı katmanlama ve domain izolasyonu.
- **Dual Authentication**: Aynı anda hem Session hem de JWT tabanlı yetkilendirme.
- **Role-Based Access Control (RBAC)**: Admin ve Kullanıcı rolleriyle güvenli erişim yönetimi.
- **Database Versioning**: Flyway ile kontrollü veritabanı şeması yönetimi.

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

## 📖 Öğrenim Çıktıları (Key Learnings)
Bu yoğun 4 günlük süreçte kodun ötesinde şu mimari tecrübeler edinilmiştir:
- **Hibrit Güvenlik**: Klasik web uygulamaları ile modern API servislerini aynı güvenlik çatısı altında (Spring Security 6) nasıl uyumlu çalıştırılacağı.
- **Hız vs Kalite Dengesi**: Kısıtlı sürede mimari kaliteden ödün vermeden nasıl ilerlenebileceği (Pragmatik DDD yaklaşımı).
- **Altyapı Otomasyonu**: Docker Compose ve GitHub Actions ile yerelden sunucuya (local-to-prod) kesintisiz bir akış kurma.
- **Sözleşme Odaklı Geliştirme**: OpenAPI (Swagger) kullanarak önyüz ve arka yüz arasındaki iletişimi standartlaştırma.

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

## 🏛️ Mimari Kararlar ve Yaklaşımlar (Architecture Decisions)

Bu proje, kısıtlı bir sürede (4 gün) hem modern standartları karşılayan hem de sürdürülebilir bir yapı kurma hedefiyle tasarlandı.

### 🏠 Neden Clean Architecture?
Portfolyo projesi olması sebebiyle, kodun çerçevelerden (Spring Boot vb.) bağımsız iş mantığını koruyabildiğini göstermek öncelikliydi. 
- **Domain Layer**: İş kuralları burada toplanarak dış dünyadan (DB, Web) izole edildi.
- **Port-Adapter Yapısı**: Veritabanı veya UI teknolojisi değişse bile iş mantığının etkilenmemesi sağlandı.

### 🔐 Çift Katmanlı Güvenlik (Dual Security Strategy)
En büyük mimari tercihlerimizden biri, aynı uygulamada hem **Session-based (Form Login)** hem de **Stateless (JWT)** yapılarını aynı anda kullanmak oldu.
- **Web UI**: Thymeleaf ile hızlıca çalışan, SEO dostu ve güvenli (CSRF korumalı) bir arayüz için Session yapısı tercih edildi.
- **API**: Gelecekte bir Angular veya Mobile uygulama eklendiğinde hazır olması için stateless JWT altyapısı kuruldu.
- **Trade-off**: İki farklı `SecurityFilterChain` yönetmek karmaşıklığı artırsa da, esneklik (flexibility) için bu maliyet göze alındı.

### ⏱️ Zaman Yönetimi ve Sprint Stratejisi
4 günlük kısıtlı sürede " çalışan ve kaliteli" bir ürün çıkarmak için şu yöntemler izlendi:
- **Pragmatik DDD**: Karmaşık Value Object yapıları yerine ilk aşamada JPA Entity ve basit DTO'lar kullanıldı (DDD-lite).
- **Manual Mapping**: MapStruct gibi kütüphanelerin konfigürasyonuyla vakit kaybetmek yerine, şeffaflık ve hata ayıklama kolaylığı için manuel mapping tercih edildi.
- **SSR-First**: Angular ile vakit kaybetmek yerine, ilk fazda Thymeleaf + Bootstrap ile çalışan bir arayüz sunularak "minimum viable product" (MVP) hedeflendi.

### ⚖️ Karşılaşılan Trade-offlar (Ödünleşimler)
1. **Veritabanı Erişimi**: Domain modelleri ile Entity'leri tamamen ayırmak yerine, geliştirme hızını artırmak için JPA Entity'leri domain katmanına yakın tutuldu.
2. **Validasyon**: Validasyon mantığı hem DTO'larda (Jakarta Validation) hem de domain seviyesinde tutularak "fail-fast" yaklaşımı benimsendi, bu da kod tekrarını bir miktar artırsa da güvenliği maksimize etti.
3. **Frontend**: Zengin bir SPA (Single Page App) yerine klasik bir Web App yapısı kuruldu; ancak API'ler tamamen decoupled (bağımsız) bırakılarak geçiş yolu açık tutuldu.
