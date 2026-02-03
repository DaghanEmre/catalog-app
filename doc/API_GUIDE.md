# 📘 API Reference Guide

**Version**: 1.0.0  
**Format**: RESTful JSON  
**Error Standard**: RFC 7807 (Problem Details)

---

## 🔐 Authentication

All API requests must include a JWT Bearer token in the `Authorization` header.

### 1. Login to Get Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

---

## 📦 Products API

### List All Products
`GET /api/products`

**Sample Request**:
```bash
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/products
```

### Create Product (Admin Only)
`POST /api/products`

**Body**:
```json
{
  "name": "MacBook Pro",
  "price": 2400.00,
  "stock": 10,
  "status": "ACTIVE"
}
```

---

## 📄 List Products (Paginated) [NEW - PR-1]

`GET /api/products/paged`

**Features**: Server-side pagination, search, filtering, and sorting for large datasets.

**Query Parameters**:
| Parameter | Type | Required | Default | Example |
|-----------|------|----------|---------|---------|
| `page` | integer | No | 0 | 0, 1, 2 |
| `size` | integer | No | 50 | 10, 50, 100 (max: 200) |
| `q` | string | No | - | "laptop", "iphone" |
| `status` | enum | No | - | "ACTIVE", "DISCONTINUED" |
| `sort` | string | No | "id,asc" | "name,asc", "price,desc" |

**Sample Requests**:
```bash
# Default pagination (page 0, 50 items)
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/api/products/paged

# Search by product name
curl -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/api/products/paged?q=Laptop"

# Filter by status
curl -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/api/products/paged?status=ACTIVE"

# Combine search + filter + sort + pagination
curl -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/api/products/paged?page=0&size=20&q=Phone&status=ACTIVE&sort=price,desc"
```

**Sample Response (200 OK)**:
```json
{
  "items": [
    {
      "id": 1,
      "name": "Laptop Dell XPS 15",
      "price": 1299.99,
      "stock": 15,
      "status": "ACTIVE",
      "formattedPrice": "$1,299.99"
    },
    {
      "id": 2,
      "name": "iPhone 15 Pro",
      "price": 999.00,
      "stock": 25,
      "status": "ACTIVE",
      "formattedPrice": "$999.00"
    }
  ],
  "totalElements": 42,
  "page": 0,
  "size": 50
}
```

**Sort Field Whitelist** (SQL Injection Protection):
- `id`, `name`, `price`, `stock`, `status`, `createdAt`, `updatedAt`
- Unknown fields are silently ignored and default to `id,asc`

**Error Responses**:
```bash
# Invalid page size (> 200)
curl -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8080/api/products/paged?size=500"
# → 400 Bad Request: "Page size must be between 1 and 200"
```


---

## ❌ Error Handling (RFC 7807)

When an error occurs, the API returns a standard "Problem Detail" object.

> [!IMPORTANT]
> **Why RFC 7807?** It provides a machine-readable format for errors, making it easier for SPAs and Mobile apps to handle validation failures programmatically.

**Sample Validation Error (400 Bad Request)**:
```json
{
  "type": "urn:problem-type:validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Request validation failed",
  "errors": {
    "price": "Price must be greater than 0",
    "name": "Product name is required"
  },
  "timestamp": "2026-01-31T23:15:00Z"
}
```

---

## 📈 Monitoring & Health

### Health Check
`GET /actuator/health`

**Sample Response**:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```
