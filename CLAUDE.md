# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

This repo is **`config-service`** — the Eureka service discovery server for the FinTrack personal finance tracker (Spring Boot 3.4 / Spring Cloud, branded "FinPlan" in the UI). It used to be the root of a Maven multi-module monorepo containing all 4 services; that monorepo was split into 4 repos, one per service:

| Repo | Contains |
|------|----------|
| **Fintrack** (this repo) | `config-service` — Eureka Server, port 8761 |
| [Core_Service_FinTrack](https://github.com/Hieule-kid/Core_Service_FinTrack) | `core` — shared library (`BaseEntity`, `ApiResponse`, `ErrorCode`, `DateUtils`) |
| [Auth_Service_FinTrack](https://github.com/Hieule-kid/Auth_Service_FinTrack) | `auth-service` — registration/login/refresh/JWT issuance, port 8081 |
| [Planning_Service_FinTrack](https://github.com/Hieule-kid/Planning_Service_FinTrack) | `planning-service` — savings plan + milestone management, port 8090 |

`auth-service` and `planning-service` each pull in `core` via a git submodule (not a Maven module in this repo) and reactor-build it together with their own code. `config-service` (this repo) does not depend on `core`. There is no API gateway — each service handles its own CORS directly via a `CorsConfigurationSource` bean.

This repo (and its 3 siblings) are **backend only**. The frontend (Next.js 16 App Router, React 19, TypeScript, Yarn) lives in a separate repo at `/Users/hieule/fintrackfe` and is not part of this checkout. It talks to these services exclusively through its own Next.js BFF layer (server actions + `/api/*` route handlers) — the browser never calls `auth-service`/`planning-service` directly, and there is no gateway in between. A fuller cross-repo architecture writeup (DB schema, ER diagram, FE structure, FE↔BE sequence diagrams) is checked in at `.agents/skills/ARCHITECTURE.md` — read it when work spans both repos or touches API contracts.

For architecture conventions shared across all 4 backend repos (layering, `BaseEntity`, error handling, DTO naming, Swagger, etc.), see `CODING_RULES.md` in this repo — it remains the single source of truth even though the services it describes now live in other repos.

## This repo's contents

- `pom.xml` — standalone POM (parent = `spring-boot-starter-parent` directly, no shared `fintrack-parent` anymore).
- `src/main/java/com/fintrack/config/` — `EurekaServerApplication`, `EurekaSecurityConfig` (HTTP Basic auth on the dashboard, `eureka`/`eureka123` locally).
- `docker-compose.yml` — Postgres + pgAdmin + this service, for full local-stack dev (the other 3 services run their own compose files alongside this one).
- `render.yaml` — deploys `fintrack-config` on Render.

## Common commands

```bash
# Infra + this service
docker compose up -d

# Or without Docker
./mvnw spring-boot:run

# Build
./mvnw clean install -DskipTests
```

Eureka registry check: `curl http://eureka:eureka123@localhost:8761/eureka/apps`.

Services must start in order: `config-service` (this repo) → `auth-service` → `planning-service` — each depends on Eureka being up. To run the full stack, also clone `Auth_Service_FinTrack` and `Planning_Service_FinTrack` and follow their own READMEs (each has its own `docker-compose.yml` with `EUREKA_HOST` pointed back at this container).

## Security model (system-wide, for context)

- Access token: JWT, 15 min, kept in memory client-side (never localStorage). Refresh token: UUID, 7 days, HttpOnly cookie, persisted in Postgres — issued/validated in `auth-service`, not here.
- `auth-service` issues tokens; `planning-service` only validates them against the shared `FINTRACK_JWT_SECRET`. Neither secret nor DB config exists in this repo.
- `config-service`'s own dashboard/`/eureka/**` endpoints are protected by HTTP Basic (`EUREKA_USERNAME`/`EUREKA_PASSWORD`), shared by convention with the other two services' Eureka client config.

## Deployment

Production runs on Render (`render.yaml`), one Docker web service per repo, `singapore` region, deployed straight from GitHub on commit — no gateway/proxy in front. This repo only deploys `fintrack-config`; the other two Render services (`fintrack-auth`, `fintrack-planning`) are deployed from their own repos.
