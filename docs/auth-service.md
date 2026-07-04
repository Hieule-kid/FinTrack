# FinTrack :: Auth Service

> Authentication and Authorization service — user registration, login, JWT issuance, token refresh, and logout.

---

## ℹ️ Overview

| Property | Value |
|---|---|
| **Port** | `8081` |
| **Database** | `fintrack_auth` (PostgreSQL) |
| **Swagger UI** | `http://localhost:8081/swagger-ui.html` |
| **OpenAPI JSON** | `http://localhost:8081/v3/api-docs` |
| **Depends on** | `config-service` (Eureka), PostgreSQL |

---

## 🚀 How to Start

```bash
# Prerequisite: config-service and PostgreSQL must be running
cd auth-service
../mvnw spring-boot:run
# ✅ Ready when you see: "Started AuthServiceApplication on port 8081"
```

**Docker:**
```bash
docker compose up auth-service
```

---

## 📁 Source Structure

```
auth-service/src/main/java/com/fintrack/auth/
├── AuthServiceApplication.java              # Entry point — @EnableJpaAuditing, @EnableScheduling
├── config/
│   ├── SecurityConfig.java                  # Spring Security filter chain (JWT, stateless)
│   ├── OpenApiConfig.java                   # Swagger UI + JWT Bearer security scheme
│   └── TokenCleanupScheduler.java           # @Scheduled — deletes expired refresh tokens daily at 02:00
├── controller/
│   ├── AuthController.java                  # /api/v1/auth/** — register, login, refresh, me, logout
│   └── UserController.java                  # /api/v1/users/profile — alternate profile endpoint
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java                # emailOrUsername + password
│   │   ├── RegisterRequest.java             # fullName + username + email + password
│   │   └── RefreshTokenRequest.java         # refreshToken string
│   └── response/
│       ├── AuthResponse.java                # accessToken + refreshToken + tokenType + expiresIn + user
│       └── UserResponse.java                # id + fullName + username + email + roles + audit fields
├── filter/
│   └── JwtAuthFilter.java                   # OncePerRequestFilter — validates Bearer JWT
├── model/
│   ├── User.java                            # @Entity users — implements UserDetails
│   ├── RefreshToken.java                    # @Entity refresh_tokens
│   ├── Account.java                         # @Entity accounts — OAuth2 linked accounts
│   └── enums/
│       └── Role.java                        # ADMIN · USER (implements GrantedAuthority)
├── repository/
│   ├── UserRepository.java
│   └── RefreshTokenRepository.java
└── service/
    ├── AuthService.java                     # Interface
    ├── JwtService.java                      # Stateless JWT util — JJWT 0.12.x, HS256
    └── impl/
        └── AuthServiceImpl.java             # Full authentication lifecycle implementation
```

---

## 🔑 API Reference

All endpoints under `http://localhost:8081`.

### Public Endpoints (no JWT required)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/auth/register` | Create a new user account |
| `POST` | `/api/v1/auth/login` | Login — returns access token + refresh token |
| `POST` | `/api/v1/auth/refresh` | Exchange a refresh token for a new access token |

### Protected Endpoints (Bearer JWT required)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/auth/me` | Get current user's profile |
| `POST` | `/api/v1/auth/logout` | Revoke all refresh tokens (all sessions) |
| `GET` | `/api/v1/users/profile` | Get current user's profile (alternate route) |

---

## 📨 Request / Response Examples

### Register

**Request** `POST /api/v1/auth/register`
```json
{
  "fullName": "Alice Smith",
  "username": "alice",
  "email": "alice@example.com",
  "password": "password123"
}
```

**Response** `201 Created`
```json
{
  "code": 201,
  "message": "Registration successful",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "Alice Smith",
    "username": "alice",
    "email": "alice@example.com",
    "roles": ["ROLE_ADMIN"],
    "createdAt": "2026-07-04T10:00:00"
  }
}
```

---

### Login

**Request** `POST /api/v1/auth/login`
```json
{
  "emailOrUsername": "alice@example.com",
  "password": "password123"
}
```
> `emailOrUsername` accepts either a **username** or **email address**.

**Response** `200 OK`
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "d1e8a8b4-...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": "550e8400-...",
      "username": "alice",
      "email": "alice@example.com",
      "roles": ["ROLE_ADMIN"]
    }
  }
}
```

---

### Refresh Token

**Request** `POST /api/v1/auth/refresh`
```json
{
  "refreshToken": "d1e8a8b4-..."
}
```

**Response** `200 OK` — same shape as login response, with a new access token and new refresh token (old one is revoked).

---

### Logout

**Request** `POST /api/v1/auth/logout`  
Header: `Authorization: Bearer <accessToken>`

**Response** `204 No Content`

---

## 🗄️ Database Schema

### `users`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | `VARCHAR(36)` | PK, UUID, immutable |
| `full_name` | `VARCHAR(100)` | nullable |
| `username` | `VARCHAR(50)` | UNIQUE, NOT NULL |
| `email` | `VARCHAR(100)` | UNIQUE, NOT NULL |
| `password_hash` | `TEXT` | NOT NULL (BCrypt) |
| `created_at` | `TIMESTAMP` | auto-set |
| `updated_at` | `TIMESTAMP` | auto-updated |
| `created_by` | `VARCHAR(36)` | nullable |
| `updated_by` | `VARCHAR(36)` | nullable |
| `is_deleted` | `BOOLEAN` | NOT NULL, default `false` |

### `user_roles`

| Column | Type |
|--------|------|
| `user_id` | `VARCHAR(36)` FK → `users.id` |
| `role` | `VARCHAR(20)` (`ADMIN`, `USER`) |

### `refresh_tokens`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | `VARCHAR(36)` | PK, UUID |
| `token` | `VARCHAR(36)` | UNIQUE, NOT NULL |
| `user_id` | `VARCHAR(36)` | NOT NULL (soft link — no FK) |
| `expiry_date` | `TIMESTAMP` | NOT NULL |

### `accounts` (OAuth2 linked accounts)

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | `VARCHAR(36)` | PK, UUID |
| `user_id` | `VARCHAR(36)` | FK → `users.id` (CASCADE DELETE) |
| `provider` | `VARCHAR(50)` | NOT NULL (e.g. `"google"`, `"github"`) |
| `provider_account_id` | `VARCHAR(255)` | NOT NULL |
| `access_token` | `TEXT` | NOT NULL |

Unique index: `(user_id, provider, provider_account_id)` — one account per user per provider.

---

## 🔐 Security Model

| Token | Lifetime | Client Storage | Notes |
|-------|----------|----------------|-------|
| Access Token | 15 min (`900 000 ms`) | Memory (JS var) — **never** `localStorage` | Stateless JWT, HS256 |
| Refresh Token | 7 days | HttpOnly cookie (recommended) | UUID stored in PostgreSQL |

**JWT Payload claims** embedded by `JwtService.generateAccessToken()`:
- `sub` — username
- `userId` — user's UUID (used by downstream services like `planning-service`)
- `username` — username
- `email` — user's email
- `roles` — list of role strings

**Spring Security filter chain** (stateless, no HTTP session):
1. Requests to `/api/v1/auth/login`, `/register`, `/refresh`, Swagger, Actuator health → **permit all**
2. All other requests → `JwtAuthFilter` extracts and validates the Bearer token
3. On valid token → `SecurityContextHolder` is populated with the `UserDetails`
4. On invalid/missing token → request continues unauthenticated → protected endpoints return `401`

---

## ⏰ Token Cleanup Scheduler

`TokenCleanupScheduler` runs every day at **02:00 AM** to delete expired refresh tokens from the database.

```
cron: "0 0 2 * * *"   →   second=0, minute=0, hour=2, every day
```

PostgreSQL does not auto-expire rows (unlike MongoDB TTL indexes), so this scheduled job is required.

---

## ⚙️ Environment Variables

| Variable | Default | Required in Prod |
|----------|---------|------------------|
| `POSTGRES_URL` | `jdbc:postgresql://localhost:5432/fintrack_auth` | ✅ |
| `POSTGRES_USERNAME` | `postgres` | ✅ |
| `POSTGRES_PASSWORD` | `postgres` | ✅ |
| `EUREKA_HOST` | `localhost` | ✅ |
| `EUREKA_USERNAME` | `eureka` | ✅ |
| `EUREKA_PASSWORD` | `eureka123` | ✅ |
| `FINTRACK_JWT_SECRET` | `fintrack-super-secret-key-change-me-in-prod-123` | ✅ **Must change** — min 32 bytes |
| `FINTRACK_JWT_ACCESS_EXPIRY_MS` | `900000` (15 min) | ⚠️ Recommended |
| `FINTRACK_JWT_REFRESH_EXPIRY_DAYS` | `7` | ⚠️ Recommended |

> 🔑 `FINTRACK_JWT_SECRET` **must be identical** in both `auth-service` and `planning-service` — planning-service only validates tokens, it does not issue them.

---

## 🧪 Quick Test

```bash
# 1. Register
curl -s -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Alice","username":"alice","email":"alice@example.com","password":"pass123"}'

# 2. Login — save the accessToken
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"emailOrUsername":"alice","password":"pass123"}' | jq -r '.data.accessToken')

# 3. Get profile
curl -s http://localhost:8081/api/v1/auth/me \
  -H "Authorization: Bearer $TOKEN"

# 4. Logout
curl -s -X POST http://localhost:8081/api/v1/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🌐 Swagger UI — How to Authenticate

1. Open `http://localhost:8081/swagger-ui.html`
2. Call `POST /api/v1/auth/login` → **Try it out** → execute
3. Copy the `accessToken` value from the response
4. Click **Authorize 🔒** (top right)
5. Enter the token — **without** the `Bearer ` prefix
6. All subsequent requests automatically include the JWT header

