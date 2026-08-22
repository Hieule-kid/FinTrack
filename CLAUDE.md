# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

FinTrack — a Spring Boot 3.4 / Spring Cloud microservices personal finance tracker (branded "FinPlan" in the UI). Java 17, Maven multi-module build, PostgreSQL, Eureka service discovery, JWT auth. No API gateway — each service handles its own CORS directly via a `CorsConfigurationSource` bean (the gateway was removed; see `docs/gateway-service.md` which is now stale).

This repo is the **backend only**. The frontend (Next.js 16 App Router, React 19, TypeScript, Yarn) lives in a sibling repo at `/Users/hieule/fintrackfe` and is not part of this checkout. It talks to these services exclusively through its own Next.js BFF layer (server actions + `/api/*` route handlers) — the browser never calls `auth-service`/`planning-service` directly, and there is no gateway in between. A fuller cross-repo architecture writeup (DB schema, ER diagram, FE structure, FE↔BE sequence diagrams) is checked in at `.agents/skills/ARCHITECTURE.md` — read it when work spans both repos or touches API contracts.

## Modules

- `pom.xml` — parent POM, single source of truth for all dependency versions (Spring Boot 3.4.4, Spring Cloud 2024.0.1, JJWT 0.12.6, MapStruct 1.6.2, springdoc 2.8.3, Spring AI 1.0.0).
- `core/` — shared library (not a runnable service). `BaseEntity`/`BaseService`/`BaseController` generic CRUD scaffolding, `ApiResponse`/`PageResponse` envelopes, `ErrorCode`/`AppException`/`GlobalExceptionHandler` for unified error handling, `DateUtils`.
- `config-service/` — Eureka Server, port 8761 (dashboard at `http://localhost:8761`, basic auth `eureka`/`eureka123` locally).
- `auth-service/` — registration/login/refresh/JWT issuance, port 8081. Owns the `fintrack_auth` DB.
- `planning-service/` — savings plan + milestone management, port 8090. Owns `fintrack_planning` DB. Only *validates* JWTs (shared `FINTRACK_JWT_SECRET`); never issues tokens.
- `service-template/` — starter template to copy when creating a new service (port 8082 convention).

Services must start in order: `config-service` → `auth-service` → `planning-service` (each depends on Eureka being up).

## Common commands

```bash
# Infra (Postgres + pgAdmin)
docker compose up -d db pgadmin

# Build all modules
./mvnw clean install -DskipTests

# Run a single service
./mvnw -pl config-service spring-boot:run
./mvnw -pl auth-service spring-boot:run
./mvnw -pl planning-service spring-boot:run

# Tests (planning-service is the module with real test coverage)
./mvnw -pl planning-service test
./mvnw -pl planning-service test -Dtest=PlanServiceImplTest
./mvnw -pl planning-service test -Dtest=PlanServiceImplTest#methodName

# Full docker stack
docker compose up -d
```

Eureka registry check: `curl http://eureka:eureka123@localhost:8761/eureka/apps`.

## Architecture conventions (enforced across all services — see CODING_RULES.md)

- **Layering is mandatory**: `controller` → `service`/`service/impl` → `repository`. No business logic in controllers, no repository access from controllers, no `@Entity` ever returned from a controller (always map to a response DTO). Constructor injection only (`@RequiredArgsConstructor`), never field `@Autowired`.
- **Every entity extends `core`'s `BaseEntity`** (UUID id, audit fields, soft delete via a `deleted` boolean — repositories must filter `...AndDeletedFalse` rather than physically deleting rows).
- **Errors flow through `core`**: throw `AppException(ErrorCode.X)` from services; never catch-and-swallow — let `GlobalExceptionHandler` (`@RestControllerAdvice`) convert it to the `ApiResponse` envelope.
- **Money is always `BigDecimal`** (never float/double); timestamps are always `LocalDateTime`.
- **DTO naming**: `Create<Domain>Request`, `Update<Domain>Request`, `<Domain>Response`. Entities/services/controllers follow `<Domain>`, `<Domain>Service`/`<Domain>ServiceImpl`, `<Domain>Controller`.
- **Config values must use `${ENV_VAR:default}`** with a production-correct default — never hardcode secrets/credentials, and dev defaults must be clearly non-production (e.g. `ddl-auto: ${JPA_DDL_AUTO:update}`, must be `validate` in prod).
- **Swagger**: every controller has `@Tag`, every endpoint `@Operation`, protected endpoints declare `@SecurityRequirement(name = "Bearer Authentication")`.
- Full detail and code examples for every rule above live in `CODING_RULES.md` — consult it for anything not summarized here (Javadoc format, logging levels, entity `@Column`/`@Index` conventions, PR checklist).

## Planning-service business rules (non-obvious, computed not stored)

- A plan's target amount is split into milestones (per day/month/year depending on `frequency`), divided evenly with the remainder on the last milestone.
- Milestone `status` is **never trusted from storage** — it's recomputed on every read from `actualSaved`, effective target, manual-completion flag, and current date, following `COMPLETED` → `OVERDUE` → `PENDING` precedence.
- When a plan's `recalculateOnMissedDeadline` setting is on, the combined deficit of overdue milestones is redistributed evenly across remaining future milestones — computed live on every read, never persisted.
- `DELETE /api/v1/plans/{planId}` is a genuine **hard delete** of the plan and its milestones (cascading) — an intentional exception to the soft-delete convention below, added after plans/milestones were found to accumulate indefinitely.

## Errors, validation, scheduling

- `ErrorCode` is numbered by domain: `1xxx` general/access (`1001 UNAUTHENTICATED`, `1002 FORBIDDEN`), `3xxx` auth/user (`3001 INVALID_CREDENTIALS`, `3002 DUPLICATE_EMAIL`), `4xxx` planning (`4001 PLAN_NOT_FOUND`, `4002 MILESTONE_NOT_FOUND`). Follow this numbering when adding new codes for a new domain.
- Both `auth-service` and `planning-service` refuse to start (`@PostConstruct` guard) if `FINTRACK_JWT_SECRET` is under 32 chars or contains the literal string `"change-me"` — a real, non-default secret is required even locally.
- `planning-service` ownership checks live in the service layer, not the controller: `getOwnedPlanOrThrow(planId, userId)` throws `FORBIDDEN (1002)` on mismatch rather than relying on a query filter.
- `MilestoneCalculator.validateCombination()` enforces legal `timeframeCategory`/`frequency`/duration-field combinations before schedule generation (e.g. `SHORT_TERM`/`MID_TERM` only allow `DAILY`/`MONTHLY` and require `durationInMonths`), and generated `periodCount` is capped to `(0, 3650]` to prevent runaway schedules.
- `TokenCleanupScheduler` (auth-service) deletes expired refresh tokens daily at 02:00 via `@Scheduled(cron=...)`. A Caffeine cache (`userProfiles`, 1000 entries / 300s TTL) backs `getProfile()`.

## Security model

- Access token: JWT, 15 min, kept in memory client-side (never localStorage). Refresh token: UUID, 7 days, HttpOnly cookie, persisted in Postgres, cleaned up daily at 02:00 by `TokenCleanupScheduler` in auth-service.
- `auth-service` issues tokens; `planning-service` (and any future service) only validates them against the shared `FINTRACK_JWT_SECRET`.

## Deployment

Production runs on Render (`render.yaml`), one Docker web service per module, `singapore` region, deployed straight from GitHub on commit — no gateway/proxy in front.
