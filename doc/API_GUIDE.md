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
