# FinTrack — Personal Finance Management System

> Microservices-based personal finance tracker — **Spring Boot 3.4** · **Spring Cloud** · **PostgreSQL** · **JWT Auth**

---

## Project Structure

```
fintrack/
├── pom.xml                          # Parent POM — single source of truth for ALL versions
│
├── config-service/                  # Service Discovery (Eureka Server) — port 8761
│   └── src/main/java/com/fintrack/config/
│       ├── EurekaServerApplication.java
│       └── EurekaSecurityConfig.java
│
├── core/                            # Shared Library — NOT a runnable service
│   └── src/main/java/com/fintrack/core/
│       ├── base/
│       │   ├── BaseEntity.java      # @MappedSuperclass — id (UUID), audit fields, soft-delete
│       │   ├── BaseService.java     # Generic CRUD contract <CreateDTO, UpdateDTO, ResponseDTO>
│       │   └── BaseController.java  # Abstract controller wiring BaseService → REST endpoints
│       ├── dto/
│       │   ├── ApiResponse.java     # Unified envelope { code, message, data, timestamp }
│       │   └── PageResponse.java    # Pagination wrapper { content, pageNumber, totalElements… }
│       ├── exception/
│       │   ├── ErrorCode.java       # All error codes (1xxx auth · 2xxx not found · 3xxx validation)
│       │   ├── AppException.java    # Runtime exception wrapping ErrorCode
│       │   └── GlobalExceptionHandler.java  # @RestControllerAdvice — converts exceptions → JSON
│       └── utils/
│           └── DateUtils.java       # Date helpers (month/week ranges, UTC, Vietnam timezone)
│
├── auth-service/                    # Authentication & Authorization — port 8081
│   └── src/main/java/com/fintrack/auth/
│       ├── config/
│       │   ├── SecurityConfig.java        # Spring Security filter chain (JWT, stateless)
│       │   ├── OpenApiConfig.java         # Swagger UI + JWT Bearer security scheme
│       │   └── TokenCleanupScheduler.java # Daily job: delete expired refresh tokens
│       ├── controller/
│       │   ├── AuthController.java        # /api/v1/auth/** (register, login, refresh, me, logout)
│       │   └── UserController.java        # /api/v1/users/profile
│       ├── filter/
│       │   └── JwtAuthFilter.java         # OncePerRequestFilter — validates Bearer JWT
│       ├── model/
│       │   ├── User.java                  # @Entity users table — implements UserDetails
│       │   ├── RefreshToken.java          # @Entity refresh_tokens table
│       │   └── enums/Role.java            # ADMIN · USER (implements GrantedAuthority)
│       └── service/
│           ├── AuthService.java
│           └── JwtService.java            # Stateless JWT util — JJWT 0.12.x
│
├── gateway-service/                 # API Gateway (Spring Cloud Gateway) — port 8080 (Docker) / 8088 (local dev)
│   └── src/main/java/com/fintrack/gateway/
│       ├── GatewayServiceApplication.java
│       └── GatewayExceptionHandler.java   # Global error handler — converts LB failures to clean JSON
│
├── planning-service/                # Savings Plan Management — port 8090
│   └── src/main/java/com/fintrack/planning/
│       └── controller/
│           └── PlanController.java        # /api/v1/plans/**
│
├── financial-service/               # Financial data — port 8083 (not yet in Docker Compose)
│
└── service-template/                # CRUD Template — copy to create new services — port 8082
```

---

## Services Overview

<<<<<<< HEAD
| Service | Port | DB | Swagger UI / Dashboard |
|--------------------|-------|-----------------------|-----------------------------------------------------|
| `config-service` | 8761 | — | `http://localhost:8761` (Eureka dashboard) |
| `gateway-service` | 8088 | — | Routes all external traffic |
| `auth-service` | 8081 | `fintrack_auth` | `http://localhost:8081/swagger-ui.html` |
| `planning-service` | 8090 | `fintrack_planning` | `http://localhost:8090/swagger-ui.html` |
| `service-template` | 8082 | `fintrack_template` | `http://localhost:8082/swagger-ui.html` |
=======
| Service | Port | DB | Notes |
|---------------------|------------------|---------------------|---------------------------------------------------|
| `config-service` | 8761 | — | Eureka dashboard: `http://localhost:8761` (eureka / eureka123) |
| `auth-service` | 8081 | `fintrack_auth` | Swagger: `http://localhost:8081/swagger-ui.html` |
| `planning-service` | 8090 | `fintrack_planning` | Swagger: `http://localhost:8090/swagger-ui.html` |
| `gateway-service` | 8080 / **8088** | — | Routes all `/api/v1/**` traffic; see port note below |
| `financial-service` | 8083 | `fintrack_financial`| Not yet wired in Docker Compose |

> > > > > > > c46ea39 (fix(gateway): add missing routes and global error handler)

### Gateway port note

In Docker the gateway runs on container port `8080`, exposed as host port `8088` (`"8088:8080"` in docker-compose).  
When running locally in IntelliJ, port `8080` is taken by pgAdmin4 (Docker). Override with a VM argument:

<<<<<<< HEAD

### ✅ Prerequisites

| Tool       | Version | Install                                             |
| ---------- | ------- | --------------------------------------------------- |
| Java JDK   | 17+     | [sdkman.io](https://sdkman.io) — `sdk install java` |
| Maven      | 3.9+    | Bundled `./mvnw`, or `brew install maven`           |
| PostgreSQL | 14+     | `brew install postgresql` or Docker (see below)     |
| Git        | any     | `brew install git`                                  |

---

### 🐳 Step 1 — Start PostgreSQL with Docker (recommended)

```bash
# Start PostgreSQL container
docker run -d \
  --name fintrack-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine

# Create the databases for each service
docker exec -it fintrack-postgres psql -U postgres -c "CREATE DATABASE fintrack_auth;"
docker exec -it fintrack-postgres psql -U postgres -c "CREATE DATABASE fintrack_planning;"
docker exec -it fintrack-postgres psql -U postgres -c "CREATE DATABASE fintrack_template;"
=======
```

-DGATEWAY_PORT=8088

> > > > > > > c46ea39 (fix(gateway): add missing routes and global error handler)

````

The `server.port` in `gateway-service/application.yml` reads `${GATEWAY_PORT:8080}` — default `8080` is safe for production/Docker, the override only applies locally.

### Gateway routes

| Route | Upstream | Path |
|-------|----------|------|
| auth-service-route | `lb://auth-service` | `/api/v1/auth/**` |
| user-service-route | `lb://auth-service` | `/api/v1/users/**` |
| planning-service-route | `lb://planning-service` | `/api/v1/plans/**` |

---

## Quick Start — How to Run Locally

### Prerequisites

| Tool       | Version | Install                                          |
|------------|---------|--------------------------------------------------|
| Java JDK   | 17+     | [sdkman.io](https://sdkman.io)                  |
| Maven      | 3.9+    | Bundled `./mvnw`                                 |
| Docker     | any     | Required for PostgreSQL + pgAdmin                |

### Step 1 — Start infrastructure (Docker)

```bash
docker compose up -d db pgadmin
````

This starts PostgreSQL on `5432` and pgAdmin on `8080`.

### Step 2 — Build all modules

```bash
./mvnw clean install -DskipTests
```

### Step 3 — Start services in order (IntelliJ or terminal)

Services must start in this exact order — each depends on Eureka being up first.

| Order | Service          | Terminal command                              | Ready when                                        |
| ----- | ---------------- | --------------------------------------------- | ------------------------------------------------- |
| 1     | config-service   | `./mvnw -pl config-service spring-boot:run`   | `Started EurekaServerApplication`                 |
| 2     | auth-service     | `./mvnw -pl auth-service spring-boot:run`     | `Started AuthServiceApplication on port 8081`     |
| 3     | planning-service | `./mvnw -pl planning-service spring-boot:run` | `Started PlanningServiceApplication on port 8090` |
| 4     | gateway-service  | `./mvnw -pl gateway-service spring-boot:run`  | `Started GatewayServiceApplication`               |

> When running gateway locally, add `-DGATEWAY_PORT=8088` (VM option in IntelliJ or `GATEWAY_PORT=8088` prefix in terminal) so it doesn't conflict with pgAdmin on port 8080.

<<<<<<< HEAD

#### Terminal 2 — Auth Service

```bash
cd auth-service
../mvnw spring-boot:run
# ✅ Ready when you see: "Started AuthServiceApplication on port 8081"
# 🌐 Swagger: http://localhost:8081/swagger-ui.html
```

#### Terminal 3 — Planning Service

```bash
cd planning-service
../mvnw spring-boot:run
# ✅ Ready when you see: "Started PlanningServiceApplication on port 8090"
# 🌐 Swagger: http://localhost:8090/swagger-ui.html
```

#### Terminal 4 — Service Template (optional)

```bash
cd service-template
../mvnw spring-boot:run
# ✅ Ready when you see: "Started TemplateServiceApplication on port 8082"
# 🌐 Swagger: http://localhost:8082/swagger-ui.html
```

---

### 🧪 Step 4 — Verify Everything Works

=======

### Step 4 — Verify

> > > > > > > c46ea39 (fix(gateway): add missing routes and global error handler)

```bash
# Eureka registry — should list AUTH-SERVICE, PLANNING-SERVICE, GATEWAY-SERVICE
curl http://eureka:eureka123@localhost:8761/eureka/apps

# Register a user
curl -X POST http://localhost:8088/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","username":"testuser","email":"test@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8088/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"emailOrUsername":"test@example.com","password":"password123"}'

# Create a plan (replace <token>)
curl -X POST http://localhost:8088/api/v1/plans \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"goalTitle":"Emergency Fund","targetAmount":10000000,"currency":"VND","durationInMonths":6}'
```

---

## Auth API Reference

| Method | Endpoint                | Auth       | Description                          |
| ------ | ----------------------- | ---------- | ------------------------------------ |
| POST   | `/api/v1/auth/register` | Public     | Create new account                   |
| POST   | `/api/v1/auth/login`    | Public     | Login → access token + refresh token |
| POST   | `/api/v1/auth/refresh`  | Public     | Exchange refresh token → new access  |
| GET    | `/api/v1/auth/me`       | Bearer JWT | Get current user profile             |
| POST   | `/api/v1/auth/logout`   | Bearer JWT | Revoke all sessions                  |
| GET    | `/api/v1/users/profile` | Bearer JWT | Get user profile (via gateway)       |

## Planning API Reference

| Method | Endpoint                                       | Auth       | Description             |
| ------ | ---------------------------------------------- | ---------- | ----------------------- |
| GET    | `/api/v1/plans`                                | Bearer JWT | List all plans          |
| POST   | `/api/v1/plans`                                | Bearer JWT | Create a plan           |
| GET    | `/api/v1/plans/{id}`                           | Bearer JWT | Get plan detail         |
| PUT    | `/api/v1/plans/{id}`                           | Bearer JWT | Update a plan           |
| DELETE | `/api/v1/plans/{id}`                           | Bearer JWT | Delete a plan           |
| PATCH  | `/api/v1/plans/{id}/milestones/{mid}`          | Bearer JWT | Update milestone amount |
| POST   | `/api/v1/plans/{id}/milestones/{mid}/complete` | Bearer JWT | Complete a milestone    |
| POST   | `/api/v1/plans/ai/generate`                    | Bearer JWT | AI plan generation      |

All endpoints above are accessible through the gateway at `localhost:8088`.

---

## Security Model

| Token         | Lifetime   | Storage (client) | Notes                     |
| ------------- | ---------- | ---------------- | ------------------------- |
| Access Token  | 15 minutes | HttpOnly cookie  | JWT — stateless           |
| Refresh Token | 7 days     | HttpOnly cookie  | UUID stored in PostgreSQL |

<<<<<<< HEAD
| Property | Value | What it controls |
|--------------------------|-------------------|-------------------------------------|
| `java.version` | `17` | Java compiler source/target |
| `spring-cloud.version` | `2024.0.1` | Spring Cloud BOM (Eureka, Gateway…) |
| `jjwt.version` | `0.12.6` | JJWT — JWT library |
| `mapstruct.version` | `1.6.2` | MapStruct bean mapper |
| `springdoc.version` | `2.8.3` | SpringDoc OpenAPI / Swagger UI |
| `fintrack.version` | `1.0.0-SNAPSHOT` | Internal module version |

**To upgrade any dependency** → change the property in the **root `pom.xml`** only.  
All modules inherit versions from the parent — never specify a version in a child module.

---

## 🔑 Auth API Reference

| Method | Endpoint                | Auth          | Description                          |
| ------ | ----------------------- | ------------- | ------------------------------------ |
| POST   | `/api/v1/auth/register` | ❌ Public     | Create new account (ROLE_USER)       |
| POST   | `/api/v1/auth/login`    | ❌ Public     | Login → access token + refresh token |
| POST   | `/api/v1/auth/refresh`  | ❌ Public     | Exchange refresh token → new access  |
| GET    | `/api/v1/auth/me`       | ✅ Bearer JWT | Get current user profile             |
| POST   | `/api/v1/auth/logout`   | ✅ Bearer JWT | Revoke all sessions                  |

---

## 🎯 Planning API Reference

`planning-service` only **validates** JWTs issued by `auth-service` (shared `FINTRACK_JWT_SECRET`) — it never issues tokens itself. Every endpoint below requires a valid JWT and is scoped to the authenticated user.

| Method | Endpoint                                                   | Description                                               |
| ------ | ---------------------------------------------------------- | --------------------------------------------------------- |
| POST   | `/api/v1/plans`                                            | Create a plan — generates the full milestone schedule     |
| GET    | `/api/v1/plans`                                            | List the current user's plans (summary)                   |
| GET    | `/api/v1/plans/{planId}`                                   | Get a plan + milestones + totals (live-recomputed status) |
| PATCH  | `/api/v1/plans/{planId}/milestones/{milestoneId}`          | Update a milestone's `actualSaved`                        |
| POST   | `/api/v1/plans/{planId}/milestones/{milestoneId}/complete` | Mark a milestone complete                                 |
| POST   | `/api/v1/plans/{planId}/milestones/{milestoneId}/undo`     | Undo a milestone's completion                             |
| PATCH  | `/api/v1/plans/{planId}/settings`                          | Toggle `recalculateOnMissedDeadline`                      |
| DELETE | `/api/v1/plans/{planId}`                                   | Delete a plan                                             |

**Business rules implemented server-side:**

- A goal is split into milestones (one per day/month/year, depending on `frequency`) — the target amount is divided evenly, with any remainder on the last milestone.
- Milestone `status` is **never** trusted from storage — it's recomputed on every read from `actualSaved`, the effective target, the manual-completion flag, and the current date: `COMPLETED` → `OVERDUE` → `PENDING`.
- When `recalculateOnMissedDeadline` is enabled, the combined deficit of all overdue (missed) milestones is redistributed evenly across the remaining future, not-yet-completed milestones — recomputed live on every read, never persisted as a one-time mutation.

---

## 🛡️ Security Model

| Token         | Lifetime   | Storage (client) | Notes                                 |
| ------------- | ---------- | ---------------- | ------------------------------------- |
| Access Token  | 15 minutes | Memory (JS var)  | JWT — stateless, **NOT** localStorage |
| Refresh Token | 7 days     | HttpOnly cookie  | UUID stored in PostgreSQL             |

- Roles: `ROLE_USER` (default on register), `ROLE_ADMIN`
- # Soft Delete: rows are **never** physically deleted — `deleted = true`
- Roles: `ROLE_USER` (default on register), `ROLE_ADMIN`
- Soft Delete: rows are never physically deleted — `deleted = true`
  > > > > > > > c46ea39 (fix(gateway): add missing routes and global error handler)
- Expired tokens: cleaned up daily at 02:00 AM by `TokenCleanupScheduler`

---

## Environment Variables

| Variable                           | Default                                 | Required in Prod                                                     |
| ---------------------------------- | --------------------------------------- | -------------------------------------------------------------------- |
| `POSTGRES_URL`                     | `jdbc:postgresql://localhost:5432/...`  | Yes                                                                  |
| `POSTGRES_USERNAME`                | `postgres`                              | Yes                                                                  |
| `POSTGRES_PASSWORD`                | `postgres`                              | Yes                                                                  |
| `EUREKA_HOST`                      | `localhost`                             | Yes                                                                  |
| `EUREKA_USERNAME`                  | `eureka`                                | Yes                                                                  |
| `EUREKA_PASSWORD`                  | `eureka123`                             | Yes                                                                  |
| `FINTRACK_JWT_SECRET`              | `fintrack-super-secret-key-...`         | **Must change**                                                      |
| `FINTRACK_JWT_ACCESS_EXPIRY_MS`    | `900000` (15 min)                       | Recommended                                                          |
| `FINTRACK_JWT_REFRESH_EXPIRY_DAYS` | `7`                                     | Recommended                                                          |
| `GATEWAY_PORT`                     | `8080`                                  | Local dev only — set to `8088` when running alongside pgAdmin Docker |
| `JPA_DDL_AUTO`                     | `update` (dev) → use `validate` in prod | Yes                                                                  |
| `HIKARI_MAX_POOL`                  | `10`                                    | Tune for prod                                                        |

---

## Standard Response Format

```json
{
  "code": 200,
  "message": "OK",
  "data": { "id": "...", "username": "..." },
  "timestamp": "2026-04-26T10:00:00"
}
```

Error response:

```json
{
  "code": 3001,
  "message": "Validation failed",
  "data": { "email": "Invalid email format" },
  "timestamp": "2026-04-26T10:00:01"
}
```

Gateway error (service unreachable):

```json
{
  "code": 503,
  "message": "Service temporarily unavailable",
  "timestamp": "2026-04-26T10:00:01"
}
```

---

<<<<<<< HEAD

## 📚 Service Documentation

Detailed Markdown docs live in the [`docs/`](docs/) folder:

| File                                                   | Covers                                                                |
| ------------------------------------------------------ | --------------------------------------------------------------------- |
| [`docs/core.md`](docs/core.md)                         | Shared library — BaseEntity, ApiResponse, ErrorCode, DateUtils        |
| [`docs/config-service.md`](docs/config-service.md)     | Eureka discovery server — startup, credentials, health check          |
| [`docs/auth-service.md`](docs/auth-service.md)         | Auth — register, login, JWT, refresh tokens, security model           |
| [`docs/planning-service.md`](docs/planning-service.md) | Planning — savings goals, milestone schedules, deficit redistribution |
| [`docs/gateway-service.md`](docs/gateway-service.md)   | API Gateway — route table, Docker config                              |

---

## 🗺️ Planned Services (Roadmap)

| Service                | Port | DB                      | Description                  |
| ---------------------- | ---- | ----------------------- | ---------------------------- |
| `transaction-service`  | 8083 | `fintrack_transaction`  | Income & expense tracking    |
| `budget-service`       | 8084 | `fintrack_budget`       | Monthly budget management    |
| `category-service`     | 8085 | `fintrack_category`     | Transaction categories       |
| `report-service`       | 8086 | `fintrack_report`       | Analytics & spending reports |
| `notification-service` | 8087 | `fintrack_notification` | Budget alerts & reminders    |

---

# _FinTrack — July 2026_

## How to Create a New Microservice from Template

```bash
# 1. Copy the template module
cp -r service-template my-new-service

# 2. Update my-new-service/pom.xml — change artifactId and name

# 3. Rename Java packages inside the module
find my-new-service/src -type f -name "*.java" \
  -exec sed -i '' 's/template/mynew/g; s/Template/MyNew/g' {} +

# 4. Register in root pom.xml <modules>

# 5. Set port and DB name in my-new-service/src/main/resources/application.yml

# 6. Create the database
psql -U postgres -c "CREATE DATABASE fintrack_mynew;"
```

---

## Version Management

All dependency versions are defined once in the root `pom.xml` `<properties>`:

| Property               | Value      | What it controls                    |
| ---------------------- | ---------- | ----------------------------------- |
| `java.version`         | `17`       | Java compiler source/target         |
| `spring-cloud.version` | `2024.0.1` | Spring Cloud BOM (Eureka, Gateway…) |
| `jjwt.version`         | `0.12.6`   | JJWT — JWT library                  |
| `mapstruct.version`    | `1.6.2`    | MapStruct bean mapper               |
| `springdoc.version`    | `2.8.3`    | SpringDoc OpenAPI / Swagger UI      |

**To upgrade any dependency** — change the property in the root `pom.xml` only. All modules inherit versions from the parent; never specify a version in a child module.

> > > > > > > c46ea39 (fix(gateway): add missing routes and global error handler)
