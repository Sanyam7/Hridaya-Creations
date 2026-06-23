# Hridaya Creations — Backend

Production-ready REST backend for **Hridaya Creations**, a personalized gifting e‑commerce platform.
Built with an enterprise, layered architecture (Controller → Service → Repository), JWT-secured
Role-Based Access Control, DTO/mapper isolation, global error handling, auditing and OpenAPI docs.

---

## Table of contents
1. [Tech stack](#tech-stack)
2. [Features](#features)
3. [Architecture & project structure](#architecture--project-structure)
4. [Prerequisites](#prerequisites)
5. [Quick start (Docker)](#quick-start-docker)
6. [Run locally](#run-locally)
7. [Configuration](#configuration)
8. [Default admin & roles](#default-admin--roles)
9. [API reference](#api-reference)
10. [Pricing model](#pricing-model)
11. [Image storage (Cloudinary)](#image-storage-cloudinary)
12. [Testing](#testing)
13. [Database](#database)
14. [Postman collection](#postman-collection)

---

## Tech stack
| Area | Technology |
|------|------------|
| Language / runtime | **Java 21** |
| Framework | **Spring Boot 3.3.x** (Web, Data JPA, Security, Validation, Mail, Actuator) |
| Persistence | **PostgreSQL** + Hibernate/JPA |
| Security | **Spring Security + JWT** (jjwt 0.12), BCrypt |
| Mapping | **MapStruct 1.6** |
| Boilerplate | **Lombok** |
| Docs | **springdoc-openapi / Swagger UI** |
| File storage | **Cloudinary** |
| Build | **Maven** (wrapper included) |
| Tests | **JUnit 5 + Mockito + Spring Test + H2** |

> **Build JDK note:** the project targets and must be built with **JDK 21**. Lombok's annotation
> processor does not run on JDK 25+, so building with a newer JDK will fail. Use the bundled
> `./mvnw` with `JAVA_HOME` pointing at a JDK 21 installation.

## Features
- **Auth**: register, login, JWT access + refresh tokens (DB-backed rotation & revocation), logout,
  forgot/reset/change password, profile management. Strong-password and confirm-password validation.
- **Catalog**: categories & products with images, tags, pricing, inventory, featured/latest, and
  dynamic search (keyword, category, price range, tag) with pagination, sorting and filtering.
- **Cart**: add / update / increment / decrement / remove / clear with live stock checks and a full
  price breakdown (subtotal, GST, delivery, total).
- **Orders**: place from cart (stock decrement + price/address snapshot), cancel (restock), track;
  admin status workflow with validated transitions (`PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED`, or `CANCELLED`).
- **Addresses, Wishlist, Reviews** (purchase-gated, one per product, drives product rating).
- **Admin**: manage categories, products, images, pricing, inventory, orders, users, and view audit logs.
- **Cross-cutting**: uniform `ApiResponse` envelope, global exception handling, JPA auditing
  (`createdAt/By`, `updatedAt/By`), audit log of significant actions, CORS, OpenAPI.

## Architecture & project structure
Clean, layered architecture with strict DTO isolation (entities are never returned by controllers).

```
com.hridayacreations
├── config           # AppProperties, security wiring beans, OpenAPI, Cloudinary, JPA auditing, DataSeeder
├── constants        # AppConstants, SecurityConstants, MessageConstants
├── controller       # REST controllers: auth, product, category, cart, order, user, admin
├── dto
│   ├── request      # validated inbound payloads
│   ├── response     # outbound payloads + ApiResponse / PagedResponse / ErrorResponse
│   └── mapper       # MapStruct entity ⇆ DTO mappers
├── entity           # JPA entities (+ enums) extending an audited BaseEntity
├── exception        # custom exceptions + GlobalExceptionHandler
├── repository       # Spring Data repositories (+ specification for product search)
├── security         # JWT provider, filter, handlers, UserDetails, SecurityConfig, SecurityUtils
├── service
│   ├── interfaces   # service contracts
│   ├── impl         # implementations
│   └── support      # OrderPricingCalculator
├── util             # MoneyUtils, PricingUtils, ReferenceGenerator, PageableBuilder
└── validator        # @StrongPassword, @FieldMatch + validators
```

## Prerequisites
- **JDK 21** (required for building — see the build-JDK note above)
- **PostgreSQL 14+** (or use Docker Compose, which provisions it)
- **Docker** + **Docker Compose** (optional, for containerized run)

## Quick start (Docker)
Builds the app and starts PostgreSQL + the backend:

```bash
cd backend
cp .env.example .env        # adjust secrets as needed
docker compose up --build
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Run locally
1. Create the database:
   ```sql
   CREATE DATABASE hridaya_creations;
   ```
2. Export configuration (or create `backend/.env` and source it). At minimum set
   `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`.
3. Build & run with the wrapper (ensure `JAVA_HOME` is a **JDK 21**):
   ```bash
   cd backend
   ./mvnw clean spring-boot:run
   ```
   On Windows PowerShell:
   ```powershell
   $env:JAVA_HOME="C:\path\to\jdk-21"
   .\mvnw.cmd clean spring-boot:run
   ```

On first boot the app seeds the roles, the default admin and (in non-prod) a sample catalog.

## Configuration
All settings are environment-variable driven (see [`.env.example`](.env.example)). Key variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` / `prod` |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | local Postgres | datasource |
| `JPA_DDL_AUTO` | `update` (`validate` in prod) | Hibernate DDL strategy |
| `JWT_SECRET` | sample (**change in prod**) | Base64 HMAC secret (≥256-bit) |
| `JWT_ACCESS_EXPIRATION` / `JWT_REFRESH_EXPIRATION` | 1h / 7d (ms) | token lifetimes |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | `admin@ridhayacreations.com` / `Admin@12345` | seeded admin |
| `CLOUDINARY_*` | demo | Cloudinary credentials; `CLOUDINARY_ENABLED=false` uses placeholders |
| `ORDER_GST_PERCENTAGE` / `ORDER_DELIVERY_CHARGE` / `ORDER_FREE_DELIVERY_THRESHOLD` | 18 / 50 / 500 | pricing |
| `CORS_ALLOWED_ORIGINS` | localhost:3000,5173 | allowed front-end origins |
| `SEED_SAMPLE_DATA` | `true` (`false` in prod) | seed sample catalog |

## Default admin & roles
Seeded automatically on startup (idempotent):

| Field | Value |
|-------|-------|
| Email | `admin@ridhayacreations.com` |
| Password | `Admin@12345` |
| Role | `ROLE_ADMIN` |

**RBAC**
- `ROLE_USER` — register, browse, search, cart, place/track/cancel orders, addresses, wishlist, reviews.
- `ROLE_ADMIN` — everything under `/api/v1/admin/**`: manage categories, products, images, pricing,
  inventory, orders, users, and view audit logs.

## API reference
- **Base path:** `/api/v1`
- **Interactive docs:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

**Auth flow:** `POST /api/v1/auth/login` → use `data.accessToken` as `Authorization: Bearer <token>`
on protected calls; refresh via `POST /api/v1/auth/refresh-token`.

| Area | Method & path | Access |
|------|---------------|--------|
| Auth | `POST /auth/register`, `/auth/login`, `/auth/refresh-token`, `/auth/logout`, `/auth/forgot-password`, `/auth/reset-password` | Public |
| Auth | `POST /auth/change-password` | User |
| Profile | `GET/PUT /users/me` | User |
| Products | `GET /products`, `/products/{id}`, `/products/featured`, `/products/latest`, `/products/{id}/reviews` | Public |
| Categories | `GET /categories`, `/categories/{id}` | Public |
| Cart | `GET /cart`, `POST /cart/items`, `PUT /cart/items/{id}`, `PATCH /cart/items/{id}/increase|decrease`, `DELETE /cart/items/{id}`, `DELETE /cart` | User |
| Orders | `POST /orders`, `GET /orders`, `GET /orders/{id}`, `POST /orders/{id}/cancel` | User |
| Addresses | `GET/POST /addresses`, `GET/PUT/DELETE /addresses/{id}` | User |
| Wishlist | `GET/POST /wishlist`, `DELETE /wishlist/products/{productId}` | User |
| Reviews | `POST /reviews`, `PUT/DELETE /reviews/{id}` | User |
| Admin · Categories | `POST/GET /admin/categories`, `PUT/DELETE /admin/categories/{id}` | Admin |
| Admin · Products | `POST/GET /admin/products`, `PUT/DELETE /admin/products/{id}`, `PATCH /admin/products/{id}/enable|disable|pricing|stock` | Admin |
| Admin · Images | `GET/POST /admin/products/{productId}/images`, `PUT/DELETE …/images/{imageId}` | Admin |
| Admin · Orders | `GET /admin/orders`, `GET /admin/orders/{id}`, `PATCH /admin/orders/{id}/status` | Admin |
| Admin · Users | `GET /admin/users`, `GET /admin/users/{id}`, `PATCH /admin/users/{id}/status` | Admin |
| Admin · Audit | `GET /admin/audit-logs` | Admin |

**Standard response envelope**
```json
{ "success": true, "message": "Product created successfully", "data": { }, "timestamp": "2026-06-22T10:15:30Z" }
```
List endpoints wrap a paginated payload in `data`: `{ content, page, size, totalElements, totalPages, first, last, empty }`.

## Pricing model
For a cart/order subtotal `S`:
- **GST** = `S × ORDER_GST_PERCENTAGE%` (default 18%)
- **Delivery** = `0` when `S ≥ ORDER_FREE_DELIVERY_THRESHOLD` (default ₹500), else `ORDER_DELIVERY_CHARGE` (default ₹50)
- **Total** = `S + GST + Delivery`

All amounts are computed with `BigDecimal`, scale 2, `HALF_UP`.

## Image storage (Cloudinary)
Admin image endpoints upload to Cloudinary and persist the secure URL + public id (for delete/replace).
With `CLOUDINARY_ENABLED=false` (default for local/dev/test) a deterministic placeholder URL is returned,
so the full flow works without credentials. Allowed types: JPEG/PNG/WEBP/GIF, max 10 MB.

## Testing
```bash
cd backend
./mvnw test          # JUnit 5 + Mockito unit tests, @WebMvcTest slice, full-context smoke test (H2)
```
Tests run against in-memory **H2** (profile `test`) and require no external services.

## Database
- Hibernate manages the schema (`JPA_DDL_AUTO`). For environments that require an explicit schema,
  a hand-written DDL is provided in [`docs/schema.sql`](docs/schema.sql), and reference seed data in
  [`docs/seed-data.sql`](docs/seed-data.sql).
- Entities carry optimistic-locking `version` and audit columns.

## Postman collection
Import [`docs/Hridaya-Creations.postman_collection.json`](docs/Hridaya-Creations.postman_collection.json).
Set the `baseUrl` variable (default `http://localhost:8080`). The **Login** request auto-saves the
returned `accessToken`/`refreshToken` into collection variables used by the other requests.

---
© Hridaya Creations. Proprietary.
