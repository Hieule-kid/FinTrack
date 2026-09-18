# FinTrack :: Config Service

> Service Discovery for the FinTrack platform — **Spring Cloud Netflix Eureka Server**, port 8761.

This repo used to be the whole FinTrack Maven monorepo. It has been split into 4 repos, one per service:

| Repo | Contains | Port |
|------|----------|------|
| **Fintrack** (this repo) | `config-service` — Eureka discovery server | 8761 |
| [Core_Service_FinTrack](https://github.com/Hieule-kid/Core_Service_FinTrack) | `core` — shared library (BaseEntity, ApiResponse, ErrorCode, DateUtils) | — |
| [Auth_Service_FinTrack](https://github.com/Hieule-kid/Auth_Service_FinTrack) | `auth-service` — registration/login/refresh/JWT issuance | 8081 |
| [Planning_Service_FinTrack](https://github.com/Hieule-kid/Planning_Service_FinTrack) | `planning-service` — savings plan + milestone management | 8090 |

`auth-service` and `planning-service` each depend on `core` via a git submodule (see their own READMEs). `config-service` has no dependency on `core`.

---

## Project Structure (this repo)

```
Fintrack/
├── pom.xml            # standalone pom (spring-boot-starter-parent directly)
├── Dockerfile
├── render.yaml         # Render deploy config for fintrack-config
├── docker-compose.yml  # local dev: Postgres + pgAdmin + config-service
└── src/main/java/com/fintrack/config/
    ├── EurekaServerApplication.java
    └── EurekaSecurityConfig.java
```

---

## Quick Start — Running the full stack locally

Services must start in this order — each depends on Eureka being up first.

### 1. Start infra + config-service (this repo)

```bash
cp .env.example .env   # fill in EUREKA_USERNAME/PASSWORD
docker compose up -d
# Eureka dashboard: http://localhost:8761 (eureka / eureka123)
```

Or without Docker:

```bash
./mvnw spring-boot:run
```

### 2. Clone and start `auth-service`

```bash
git clone --recurse-submodules https://github.com/Hieule-kid/Auth_Service_FinTrack.git
cd Auth_Service_FinTrack
cp .env.example .env   # FINTRACK_JWT_SECRET must be a real secret, matching planning-service
docker compose up -d
```

### 3. Clone and start `planning-service`

```bash
git clone --recurse-submodules https://github.com/Hieule-kid/Planning_Service_FinTrack.git
cd Planning_Service_FinTrack
cp .env.example .env   # same FINTRACK_JWT_SECRET as auth-service
docker compose up -d
```

### 4. Verify

```bash
# Eureka registry — should list AUTH-SERVICE, PLANNING-SERVICE
curl http://eureka:eureka123@localhost:8761/eureka/apps

# Register a user
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","username":"testuser","email":"test@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"emailOrUsername":"test@example.com","password":"password123"}'

# Create a plan (replace <token>)
curl -X POST http://localhost:8090/api/v1/plans \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"goalTitle":"Emergency Fund","targetAmount":10000000,"currency":"VND","durationInMonths":6}'
```

---

## Security Model

| Token | Lifetime | Storage (client) | Notes |
|-------|----------|-----------------|-------|
| Access Token | 15 minutes | Memory (JS var) | JWT — stateless, **NOT** localStorage |
| Refresh Token | 7 days | HttpOnly cookie | UUID stored in PostgreSQL |

- Soft Delete: rows are never physically deleted (`deleted = true`) — except plans/milestones, which are hard-deleted on `DELETE /api/v1/plans/{planId}`.
- Expired refresh tokens: cleaned up daily at 02:00 by `TokenCleanupScheduler` (in `auth-service`).
- CORS: each service handles its own CORS via a `CorsConfigurationSource` bean; allowed origin controlled by `FRONTEND_ORIGIN` env var (set on `auth-service`/`planning-service`, not needed here).

---

## Environment Variables (this repo)

| Variable | Default | Required in Prod |
|----------|---------|-----------------|
| `EUREKA_USERNAME` | `eureka` | Yes |
| `EUREKA_PASSWORD` | `eureka123` | Yes |

`FINTRACK_JWT_SECRET`, `POSTGRES_*`, `GEMINI_API_KEY`, `FRONTEND_ORIGIN` are set on `auth-service`/`planning-service` in their own repos — see each repo's README/`.env.example`.

---

## Service Documentation

Detailed Markdown docs live in [`docs/`](docs/):

| File | Covers |
|------|--------|
| [`docs/config-service.md`](docs/config-service.md) | Eureka discovery server — startup, credentials, health check |

Docs for `core`, `auth-service`, and `planning-service` now live in their own repos' READMEs.

---

## Deployment

Production runs on Render (`render.yaml`), one Docker web service per repo, `singapore` region, deployed straight from GitHub on commit — no gateway/proxy in front. Each service is a separate Render service pointed at its own repo (`fintrack-config` at this repo, `fintrack-auth`/`fintrack-planning` at their own repos).
