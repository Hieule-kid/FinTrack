# FinTrack — Personal Finance Management System

> Microservices-based personal finance tracker — **Spring Boot 3.4** · **Spring Cloud** · **PostgreSQL** · **JWT Auth**

---

## 📁 Project Structure

```
fintrack/
├── pom.xml                          # Parent POM — single source of truth for ALL versions
│
├── config-service/                  # 🔧 Service Discovery (Eureka Server) — port 8761
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/fintrack/config/
│       │   ├── EurekaServerApplication.java
│       │   └── EurekaSecurityConfig.java
│       └── resources/application.yml
│
├── gateway-service/                 # 🌐 API Gateway (Spring Cloud Gateway) — port 8088
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/fintrack/gateway/
│       │   └── GatewayServiceApplication.java
│       └── resources/application.yml
│
├── core/                            # 📦 Shared Library — NOT a runnable service
│   ├── pom.xml
│   └── src/main/java/com/fintrack/core/
│       ├── base/
│       │   ├── BaseEntity.java      # @MappedSuperclass — id (UUID), audit fields, soft-delete
│       │   ├── BaseService.java     # Generic CRUD contract <CreateDTO, UpdateDTO, ResponseDTO>
│       │   └── BaseController.java  # Abstract controller wiring BaseService → REST endpoints
│       ├── dto/
│       │   ├── ApiResponse.java     # Unified envelope  { code, message, data, timestamp }
│       │   └── PageResponse.java    # Pagination wrapper { content, pageNumber, totalElements… }
│       ├── exception/
│       │   ├── ErrorCode.java       # All error codes (1xxx auth · 2xxx not found · 3xxx validation)
│       │   ├── AppException.java    # Runtime exception wrapping ErrorCode
│       │   └── GlobalExceptionHandler.java  # @RestControllerAdvice — converts exceptions → JSON
│       └── utils/
│           └── DateUtils.java       # Date helpers (month/week ranges, UTC, Vietnam timezone)
│
├── auth-service/                    # 🔐 Authentication & Authorization — port 8081
│   ├── pom.xml
│   └── src/main/java/com/fintrack/auth/
│       ├── AuthServiceApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java        # Spring Security filter chain (JWT, stateless)
│       │   ├── OpenApiConfig.java         # Swagger UI + JWT Bearer security scheme
│       │   └── TokenCleanupScheduler.java # Daily job: delete expired refresh tokens
│       ├── controller/
│       │   ├── AuthController.java        # /api/v1/auth/** (register, login, refresh, me, logout)
│       │   └── UserController.java        # /api/v1/users/profile
│       ├── dto/
│       │   ├── request/   # LoginRequest · RegisterRequest · RefreshTokenRequest
│       │   └── response/  # AuthResponse · UserResponse
│       ├── filter/
│       │   └── JwtAuthFilter.java   # OncePerRequestFilter — validates Bearer JWT
│       ├── model/
│       │   ├── User.java            # @Entity users table — implements UserDetails
│       │   ├── RefreshToken.java    # @Entity refresh_tokens table
│       │   ├── Account.java         # @Entity accounts table — OAuth2 linked accounts
│       │   └── enums/Role.java      # ADMIN · USER (implements GrantedAuthority)
│       ├── repository/
│       │   ├── UserRepository.java
│       │   └── RefreshTokenRepository.java
│       └── service/
│           ├── AuthService.java
│           ├── JwtService.java      # Stateless JWT util — JJWT 0.12.x
│           └── impl/AuthServiceImpl.java
│
├── planning-service/                # 🎯 Financial Planning (savings goals) — port 8090
│   ├── pom.xml
│   └── src/main/java/com/fintrack/planning/
│       ├── PlanningServiceApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java        # Spring Security filter chain (JWT validation only)
│       │   ├── OpenApiConfig.java         # Swagger UI + JWT security scheme
│       │   └── JpaAuditingConfig.java     # @EnableJpaAuditing
│       ├── controller/
│       │   └── PlanController.java        # /api/v1/plans/** (CRUD + milestone operations)
│       ├── dto/
│       │   ├── request/   # CreatePlanRequest · UpdateMilestoneRequest · ToggleRecalculateRequest
│       │   └── response/  # PlanResponse · PlanSummaryResponse · MilestoneResponse
│       ├── filter/
│       │   └── JwtAuthFilter.java   # Validates JWT, extracts userId claim
│       ├── model/
│       │   ├── PlanEntity.java        # @Entity plans table
│       │   ├── MilestoneEntity.java   # @Entity milestones table (FK to PlanEntity)
│       │   └── enums/  # TimeframeCategory · Frequency · MilestoneStatus
│       ├── repository/
│       │   ├── PlanRepository.java
│       │   └── MilestoneRepository.java
│       └── service/
│           ├── PlanService.java
│           ├── JwtService.java      # Stateless, validation-only JWT util (never issues tokens)
│           └── impl/
│               ├── PlanServiceImpl.java
│               └── MilestoneCalculator.java  # Pure math: schedule gen, live status, deficit redistribution
│
└── service-template/                # 📋 CRUD Template — copy to create new services — port 8082
    ├── pom.xml
    └── src/main/java/com/fintrack/template/
        ├── TemplateServiceApplication.java
        ├── config/OpenApiConfig.java
        ├── controller/TemplateController.java
        ├── dto/request/   # CreateTemplateRequest · UpdateTemplateRequest
        ├── dto/response/  # TemplateResponse
        ├── model/TemplateEntity.java
        ├── repository/TemplateRepository.java
        └── service/
            ├── TemplateService.java
            └── impl/TemplateServiceImpl.java
```

---

## 🚀 Services Overview

| Service            | Port  | DB                    | Swagger UI / Dashboard                              |
|--------------------|-------|-----------------------|-----------------------------------------------------|
| `config-service`   | 8761  | —                     | `http://localhost:8761` (Eureka dashboard)          |
| `gateway-service`  | 8088  | —                     | Routes all external traffic                         |
| `auth-service`     | 8081  | `fintrack_auth`       | `http://localhost:8081/swagger-ui.html`             |
| `planning-service` | 8090  | `fintrack_planning`   | `http://localhost:8090/swagger-ui.html`             |
| `service-template` | 8082  | `fintrack_template`   | `http://localhost:8082/swagger-ui.html`             |

---

## ⚡ Quick Start — How to Run

### ✅ Prerequisites

| Tool         | Version   | Install                                              |
|--------------|-----------|------------------------------------------------------|
| Java JDK     | 17+       | [sdkman.io](https://sdkman.io) — `sdk install java` |
| Maven        | 3.9+      | Bundled `./mvnw`, or `brew install maven`            |
| PostgreSQL   | 14+       | `brew install postgresql` or Docker (see below)     |
| Git          | any       | `brew install git`                                   |

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
```

> **Local PostgreSQL?** Just run the `CREATE DATABASE` commands above in your local `psql` client.

---

### 🏗️ Step 2 — Build All Modules

```bash
# Clone and enter the project
git clone <repo-url>
cd fintrack

# Build all modules (skipping tests for first run)
./mvnw clean install -DskipTests
```

---

### 🟢 Step 3 — Start Services (ORDER MATTERS)

Services must start in this exact order because `auth-service` needs Eureka to be up first.

#### Terminal 1 — Config Service (Eureka)
```bash
cd config-service
../mvnw spring-boot:run
# ✅ Ready when you see: "Started EurekaServerApplication"
# 🌐 Dashboard: http://localhost:8761  (user: eureka / pass: eureka123)
```

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

```bash
# 1. Check Eureka — should list registered services
curl http://eureka:eureka123@localhost:8761/eureka/apps

# 2. Register a user
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'

# 3. Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier": "test@example.com", "password": "password123"}'

# 4. Get profile (replace <token> with accessToken from login)
curl http://localhost:8081/api/v1/auth/me \
  -H "Authorization: Bearer <token>"
```

---

### 🔐 Swagger UI — How to Authenticate

1. Open `http://localhost:8081/swagger-ui.html`
2. Click `POST /api/v1/auth/login` → **Try it out** → execute
3. Copy the `accessToken` from the response
4. Click **Authorize 🔒** (top right corner)
5. Paste the token (just the token value, **without** `Bearer `)
6. All subsequent requests will include the JWT automatically

---

## ⚙️ Version Management

All dependency versions are defined **once** in the root `pom.xml` `<properties>`:

| Property                 | Value             | What it controls                    |
|--------------------------|-------------------|-------------------------------------|
| `java.version`           | `17`              | Java compiler source/target         |
| `spring-cloud.version`   | `2024.0.1`        | Spring Cloud BOM (Eureka, Gateway…) |
| `jjwt.version`           | `0.12.6`          | JJWT — JWT library                  |
| `mapstruct.version`      | `1.6.2`           | MapStruct bean mapper               |
| `springdoc.version`      | `2.8.3`           | SpringDoc OpenAPI / Swagger UI      |
| `fintrack.version`       | `1.0.0-SNAPSHOT`  | Internal module version             |

**To upgrade any dependency** → change the property in the **root `pom.xml`** only.  
All modules inherit versions from the parent — never specify a version in a child module.

---

## 🔑 Auth API Reference

| Method | Endpoint                | Auth         | Description                         |
|--------|-------------------------|--------------|-------------------------------------|
| POST   | `/api/v1/auth/register` | ❌ Public    | Create new account (ROLE_USER)      |
| POST   | `/api/v1/auth/login`    | ❌ Public    | Login → access token + refresh token|
| POST   | `/api/v1/auth/refresh`  | ❌ Public    | Exchange refresh token → new access |
| GET    | `/api/v1/auth/me`       | ✅ Bearer JWT | Get current user profile            |
| POST   | `/api/v1/auth/logout`   | ✅ Bearer JWT | Revoke all sessions                 |

---

## 🎯 Planning API Reference

`planning-service` only **validates** JWTs issued by `auth-service` (shared `FINTRACK_JWT_SECRET`) — it never issues tokens itself. Every endpoint below requires a valid JWT and is scoped to the authenticated user.

| Method | Endpoint                                                     | Description                                             |
|--------|---------------------------------------------------------------|-----------------------------------------------------------|
| POST   | `/api/v1/plans`                                                | Create a plan — generates the full milestone schedule     |
| GET    | `/api/v1/plans`                                                | List the current user's plans (summary)                   |
| GET    | `/api/v1/plans/{planId}`                                       | Get a plan + milestones + totals (live-recomputed status) |
| PATCH  | `/api/v1/plans/{planId}/milestones/{milestoneId}`              | Update a milestone's `actualSaved`                         |
| POST   | `/api/v1/plans/{planId}/milestones/{milestoneId}/complete`     | Mark a milestone complete                                  |
| POST   | `/api/v1/plans/{planId}/milestones/{milestoneId}/undo`         | Undo a milestone's completion                              |
| PATCH  | `/api/v1/plans/{planId}/settings`                               | Toggle `recalculateOnMissedDeadline`                       |
| DELETE | `/api/v1/plans/{planId}`                                       | Delete a plan                                              |

**Business rules implemented server-side:**
- A goal is split into milestones (one per day/month/year, depending on `frequency`) — the target amount is divided evenly, with any remainder on the last milestone.
- Milestone `status` is **never** trusted from storage — it's recomputed on every read from `actualSaved`, the effective target, the manual-completion flag, and the current date: `COMPLETED` → `OVERDUE` → `PENDING`.
- When `recalculateOnMissedDeadline` is enabled, the combined deficit of all overdue (missed) milestones is redistributed evenly across the remaining future, not-yet-completed milestones — recomputed live on every read, never persisted as a one-time mutation.

---

## 🛡️ Security Model

| Token          | Lifetime    | Storage (client)    | Notes                                  |
|----------------|-------------|---------------------|----------------------------------------|
| Access Token   | 15 minutes  | Memory (JS var)     | JWT — stateless, **NOT** localStorage  |
| Refresh Token  | 7 days      | HttpOnly cookie     | UUID stored in PostgreSQL              |

- Roles: `ROLE_USER` (default on register), `ROLE_ADMIN`  
- Soft Delete: rows are **never** physically deleted — `deleted = true`  
- Expired tokens: cleaned up daily at 02:00 AM by `TokenCleanupScheduler`

---

## 🏗️ How to Create a New Microservice from Template

```bash
# 1. Copy the template module
cp -r service-template transaction-service
```

```xml
<!-- 2. Update transaction-service/pom.xml -->
<artifactId>transaction-service</artifactId>
<name>FinTrack :: Transaction Service</name>
```

```bash
# 3. Find & Replace inside transaction-service/
#    "template" → "transaction" (case-sensitive in Java files)
#    "Template" → "Transaction"
find transaction-service/src -type f -name "*.java" \
  -exec sed -i '' 's/template/transaction/g; s/Template/Transaction/g' {} +
```

```xml
<!-- 4. Register in root pom.xml -->
<modules>
  ...
  <module>transaction-service</module>
</modules>
```

```yaml
# 5. Update transaction-service/src/main/resources/application.yml
spring:
  application:
    name: transaction-service
  datasource:
    url: jdbc:postgresql://localhost:5432/fintrack_transaction
server:
  port: 8083
```

```bash
# 6. Create the new database
psql -U postgres -c "CREATE DATABASE fintrack_transaction;"

# 7. Build & Run
cd transaction-service && ../mvnw spring-boot:run
```

---

## 🌍 Environment Variables

| Variable                        | Default                                  | Required in Prod |
|---------------------------------|------------------------------------------|------------------|
| `POSTGRES_URL`                  | `jdbc:postgresql://localhost:5432/...`   | ✅ Yes           |
| `POSTGRES_USERNAME`             | `postgres`                               | ✅ Yes           |
| `POSTGRES_PASSWORD`             | `postgres`                               | ✅ Yes           |
| `EUREKA_HOST`                   | `localhost`                              | ✅ Yes           |
| `EUREKA_USERNAME`               | `eureka`                                 | ✅ Yes           |
| `EUREKA_PASSWORD`               | `eureka123`                              | ✅ Yes           |
| `FINTRACK_JWT_SECRET`           | `fintrack-super-secret-key-...`          | ✅ **Must change**|
| `FINTRACK_JWT_ACCESS_EXPIRY_MS` | `900000` (15 min)                        | ⚠️ Recommended   |
| `FINTRACK_JWT_REFRESH_EXPIRY_DAYS` | `7`                                   | ⚠️ Recommended   |
| `JPA_DDL_AUTO`                  | `update` (dev) → use `validate` in prod  | ✅ Yes           |
| `HIKARI_MAX_POOL`               | `10`                                     | ⚠️ Tune for prod |

---

## 📐 Standard Response Format

Every endpoint returns this JSON envelope:

```json
{
  "code":      200,
  "message":   "OK",
  "data":      { "id": "...", "username": "..." },
  "timestamp": "2026-04-26T10:00:00"
}
```

Paginated list:
```json
{
  "code": 200,
  "data": {
    "content":       [ { "id": "..." }, { "id": "..." } ],
    "pageNumber":    0,
    "pageSize":      10,
    "totalElements": 42,
    "totalPages":    5,
    "last":          false
  }
}
```

Error response:
```json
{
  "code":      3001,
  "message":   "Validation failed",
  "data":      { "email": "Invalid email format", "password": "Must contain at least one digit" },
  "timestamp": "2026-04-26T10:00:01"
}
```

---

## 📚 Service Documentation

Detailed Markdown docs live in the [`docs/`](docs/) folder:

| File | Covers |
|------|--------|
| [`docs/core.md`](docs/core.md) | Shared library — BaseEntity, ApiResponse, ErrorCode, DateUtils |
| [`docs/config-service.md`](docs/config-service.md) | Eureka discovery server — startup, credentials, health check |
| [`docs/auth-service.md`](docs/auth-service.md) | Auth — register, login, JWT, refresh tokens, security model |
| [`docs/planning-service.md`](docs/planning-service.md) | Planning — savings goals, milestone schedules, deficit redistribution |
| [`docs/gateway-service.md`](docs/gateway-service.md) | API Gateway — route table, Docker config |

---

## 🗺️ Planned Services (Roadmap)

| Service                | Port  | DB                      | Description                      |
|------------------------|-------|-------------------------|----------------------------------|
| `transaction-service`  | 8083  | `fintrack_transaction`  | Income & expense tracking         |
| `budget-service`       | 8084  | `fintrack_budget`       | Monthly budget management         |
| `category-service`     | 8085  | `fintrack_category`     | Transaction categories            |
| `report-service`       | 8086  | `fintrack_report`       | Analytics & spending reports      |
| `notification-service` | 8087  | `fintrack_notification` | Budget alerts & reminders         |

---

*FinTrack — July 2026*
