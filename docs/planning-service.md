# FinTrack :: Planning Service

> Personal savings goal tracking — create goals, auto-generate milestone schedules, track progress, and optionally redistribute missed-deadline deficits.

---

## ℹ️ Overview

| Property         | Value                                                                  |
| ---------------- | ---------------------------------------------------------------------- |
| **Port**         | `8090`                                                                 |
| **Database**     | `fintrack_planning` (PostgreSQL)                                       |
| **Swagger UI**   | `http://localhost:8090/swagger-ui.html`                                |
| **OpenAPI JSON** | `http://localhost:8090/v3/api-docs`                                    |
| **Depends on**   | `config-service` (Eureka), PostgreSQL, `auth-service` (for JWT secret) |

> This service **only validates** JWTs — it never issues them. The signing secret (`FINTRACK_JWT_SECRET`) must be identical to the one used by `auth-service`.

---

## 🎯 Current Business Feature State

The planning service currently supports one focused business flow: a user creates a personal savings goal, receives a full milestone schedule immediately, updates savings progress over time, and can optionally push missed amounts forward into future milestones.

### What the feature does today

- Create a savings plan for one authenticated user only; every read/write is ownership-scoped by `userId` from JWT.
- Support 2 planning horizons:
  - `SHORT_TERM`: `3-12` months with `DAILY` or `MONTHLY` milestones
  - `LONG_TERM`: `1-30` years with `MONTHLY` or `ANNUALLY` milestones
- Generate the entire milestone schedule up front at plan creation time.
- Let the user track progress by updating `actualSaved` per milestone.
- Let the user manually complete or undo completion for a milestone.
- Optionally redistribute missed overdue amounts into future pending milestones using `recalculateOnMissedDeadline`.
- Return plan-level progress (`totalSaved`, `remaining`, `progressPercent`) on both list and detail views.

### Current business behavior that matters

- Milestone `status` is not trusted from the database; it is recomputed live on every response.
- Redistributed targets are also computed live on read; the service does not persist recalculated future targets.
- Completing a milestone uses the milestone's current live target, not only its original base target.
- Undoing completion only clears the manual completion flag; it does not reset `actualSaved`.
- Deleting a plan soft-deletes the plan record, but permanently removes its milestones.

### Current implementation limits

- `requiredPerPeriod` is stored and returned in API responses, but the actual milestone allocation is derived from `targetAmount / numberOfMilestones`; it is not used as the schedule-generation source of truth.
- Monthly milestones are aligned to calendar months, and annual milestones are aligned to full calendar years. The original `startDate` is stored on the plan, but monthly and annual milestone `periodDate` values start at the first day of the corresponding month or year.
- Plan totals count `actualSaved` only from milestones currently considered `COMPLETED`. Savings entered on pending or overdue milestones do not contribute to `totalSaved` until the milestone becomes completed.

---

## 🚀 How to Start

```bash
# Prerequisite: config-service and PostgreSQL must be running
cd planning-service
../mvnw spring-boot:run
# ✅ Ready when you see: "Started PlanningServiceApplication on port 8090"
```

**Docker:**

```bash
docker compose up planning-service
```

---

## 📁 Source Structure

```
planning-service/src/main/java/com/fintrack/planning/
├── PlanningServiceApplication.java          # Entry point — @EnableDiscoveryClient
├── config/
│   ├── SecurityConfig.java                  # JWT validation-only, stateless, custom 401 handler
│   ├── JpaAuditingConfig.java               # @EnableJpaAuditing (separate class for @WebMvcTest compat)
│   └── OpenApiConfig.java                   # Swagger UI + JWT security scheme
├── controller/
│   └── PlanController.java                  # /api/v1/plans/** — full CRUD + milestone operations
├── dto/
│   ├── request/
│   │   ├── CreatePlanRequest.java           # Goal title, target amount, timeframe, frequency, start date
│   │   ├── UpdateMilestoneRequest.java      # actualSaved amount
│   │   └── ToggleRecalculateRequest.java    # recalculateOnMissedDeadline flag
│   └── response/
│       ├── PlanResponse.java                # Full plan + milestones + totals
│       ├── PlanSummaryResponse.java         # Lightweight plan (no milestone list)
│       └── MilestoneResponse.java           # Single milestone with live status + target
├── filter/
│   └── JwtAuthFilter.java                   # Extracts userId claim from Bearer JWT
├── model/
│   ├── PlanEntity.java                      # @Entity plans table
│   ├── MilestoneEntity.java                 # @Entity milestones table (FK → plans)
│   └── enums/
│       ├── TimeframeCategory.java           # SHORT_TERM · LONG_TERM
│       ├── Frequency.java                   # DAILY · MONTHLY · ANNUALLY
│       └── MilestoneStatus.java             # PENDING · OVERDUE · COMPLETED
├── repository/
│   ├── PlanRepository.java
│   └── MilestoneRepository.java
└── service/
    ├── PlanService.java                     # Interface
    ├── JwtService.java                      # Validation-only JWT util (never issues tokens)
    └── impl/
        ├── PlanServiceImpl.java             # Business logic + mapping
        └── MilestoneCalculator.java         # Pure-math: schedule generation, live status, deficit redistribution
```

---

## 🔑 API Reference

All endpoints require `Authorization: Bearer <accessToken>` and are scoped to the authenticated user.

Base URL: `http://localhost:8090`

| Method   | Path                                                       | Description                                                    |
| -------- | ---------------------------------------------------------- | -------------------------------------------------------------- |
| `POST`   | `/api/v1/plans`                                            | Create a new savings plan and auto-generate milestone schedule |
| `GET`    | `/api/v1/plans`                                            | List the current user's plans (summary — no milestone list)    |
| `GET`    | `/api/v1/plans/{planId}`                                   | Get a plan with full milestone schedule and totals             |
| `PATCH`  | `/api/v1/plans/{planId}/milestones/{milestoneId}`          | Update a milestone's `actualSaved` amount                      |
| `POST`   | `/api/v1/plans/{planId}/milestones/{milestoneId}/complete` | Mark a milestone as complete                                   |
| `POST`   | `/api/v1/plans/{planId}/milestones/{milestoneId}/undo`     | Undo a milestone's manual completion                           |
| `PATCH`  | `/api/v1/plans/{planId}/settings`                          | Toggle `recalculateOnMissedDeadline`                           |
| `DELETE` | `/api/v1/plans/{planId}`                                   | Soft-delete a plan and physically remove its milestones        |

---

## 📨 Request / Response Examples

### Create a Plan

**Request** `POST /api/v1/plans`

```json
{
  "goalTitle": "Emergency Fund",
  "targetAmount": 12000.0,
  "timeframeCategory": "SHORT_TERM",
  "durationInMonths": 6,
  "frequency": "MONTHLY",
  "requiredPerPeriod": 2000.0,
  "startDate": "2026-07-01"
}
```

> **Valid combinations:**
>
> | `timeframeCategory` | Valid `frequency`       | Required duration field   |
> | ------------------- | ----------------------- | ------------------------- |
> | `SHORT_TERM`        | `DAILY` or `MONTHLY`    | `durationInMonths` (3–12) |
> | `LONG_TERM`         | `MONTHLY` or `ANNUALLY` | `durationInYears` (1–30)  |

**Response** `201 Created`

```json
{
  "code": 201,
  "message": "Created",
  "data": {
    "id": "plan-uuid",
    "goalTitle": "Emergency Fund",
    "targetAmount": 12000.0,
    "timeframeCategory": "SHORT_TERM",
    "durationInMonths": 6,
    "frequency": "MONTHLY",
    "requiredPerPeriod": 2000.0,
    "startDate": "2026-07-01",
    "recalculateOnMissedDeadline": false,
    "createdAt": "2026-07-04T10:00:00",
    "milestones": [
      {
        "id": "m1-uuid",
        "planId": "plan-uuid",
        "sequenceIndex": 0,
        "timeline": "July 2026",
        "periodDate": "2026-07-01",
        "deadline": "2026-07-31",
        "baseTargetSavings": 2000.0,
        "targetSavings": 2000.0,
        "actualSaved": 0.0,
        "status": "PENDING"
      }
    ],
    "totalSaved": 0.0,
    "remaining": 12000.0,
    "progressPercent": 0.0
  }
}
```

---

### Update a Milestone's Actual Saved

**Request** `PATCH /api/v1/plans/{planId}/milestones/{milestoneId}`

```json
{
  "actualSaved": 1500.0
}
```

---

### Toggle Deficit Redistribution

**Request** `PATCH /api/v1/plans/{planId}/settings`

```json
{
  "recalculateOnMissedDeadline": true
}
```

---

## 💡 Core Business Rules

### Milestone Schedule Generation

When a plan is created, the full schedule is generated automatically:

- **DAILY** (`SHORT_TERM`): One milestone per day — `durationInMonths` months of days
- **MONTHLY** (`SHORT_TERM`): One milestone per calendar month — `durationInMonths` milestones
- **MONTHLY** (`LONG_TERM`): One milestone per calendar month — `durationInYears × 12` milestones
- **ANNUALLY** (`LONG_TERM`): One milestone per year — `durationInYears` milestones

Current date behavior in the implementation:

- **DAILY** milestones use the exact `startDate` and advance day by day until `startDate + durationInMonths`
- **MONTHLY** milestones use the first day and last day of each calendar month
- **ANNUALLY** milestones use `January 1` to `December 31` for each year bucket

The `targetAmount` is split evenly across all milestones (floor-divided), with any remainder added to the **last** milestone.

---

### Live Status Computation

Milestone `status` is **never** read from the database. It is recomputed on every read from three inputs:

```
COMPLETED   ← if manuallyCompleted = true  OR  actualSaved ≥ effectiveTarget
OVERDUE     ← else if deadline < today
PENDING     ← else
```

---

### Deficit Redistribution (`recalculateOnMissedDeadline`)

When enabled, overdue milestones' deficit is redistributed to future pending milestones — computed live on every read, never persisted:

1. **Deficit pool** = Σ `max(0, baseTargetSavings − actualSaved)` for all `OVERDUE` milestones
2. **Receivers** = all `PENDING` milestones (deadline not yet passed, not completed)
3. **Share** = `floor(pool / receiverCount)` added to each receiver's `targetSavings`
4. **Remainder** = added to the last receiver

If there are no future pending milestones, the deficit pool is left unredistributed.

---

## 📌 Business Notes For Product / Frontend

- The list endpoint is a summary view only; milestone details require the detail endpoint.
- The detail endpoint is the source of truth for live milestone status and effective target values.
- Frontend clients should not derive completion from persisted `status`; they should trust the response payload.
- If product expects `requiredPerPeriod` to control milestone target amounts directly, that is not how the current service behaves.
- If product expects partial savings on overdue or pending milestones to increase dashboard totals immediately, that is also not the current behavior.

---

## 🗄️ Database Schema

### `plans`

| Column                           | Type            | Constraints                                           |
| -------------------------------- | --------------- | ----------------------------------------------------- |
| `id`                             | `VARCHAR(36)`   | PK, UUID                                              |
| `user_id`                        | `VARCHAR(36)`   | NOT NULL, indexed (soft link — no FK to auth-service) |
| `goal_title`                     | `VARCHAR(50)`   | NOT NULL                                              |
| `target_amount`                  | `DECIMAL(19,2)` | NOT NULL                                              |
| `timeframe_category`             | `VARCHAR(20)`   | NOT NULL (`SHORT_TERM` / `LONG_TERM`)                 |
| `duration_in_months`             | `INTEGER`       | nullable (SHORT_TERM only)                            |
| `duration_in_years`              | `INTEGER`       | nullable (LONG_TERM only)                             |
| `frequency`                      | `VARCHAR(20)`   | NOT NULL (`DAILY` / `MONTHLY` / `ANNUALLY`)           |
| `required_per_period`            | `DECIMAL(19,2)` | NOT NULL                                              |
| `start_date`                     | `DATE`          | NOT NULL                                              |
| `recalculate_on_missed_deadline` | `BOOLEAN`       | NOT NULL, default `false`                             |
| `created_at`                     | `TIMESTAMP`     | auto-set                                              |
| `updated_at`                     | `TIMESTAMP`     | auto-updated                                          |
| `is_deleted`                     | `BOOLEAN`       | NOT NULL, default `false`                             |

### `milestones`

| Column                | Type            | Constraints                                        |
| --------------------- | --------------- | -------------------------------------------------- |
| `id`                  | `VARCHAR(36)`   | PK, UUID                                           |
| `plan_id`             | `VARCHAR(36)`   | FK → `plans.id` (`fk_milestones_plan_id`), indexed |
| `sequence_index`      | `INTEGER`       | NOT NULL (zero-based order)                        |
| `timeline`            | `VARCHAR(50)`   | NOT NULL (e.g. `"July 2026"`, `"Day 1"`)           |
| `period_date`         | `DATE`          | NOT NULL                                           |
| `deadline`            | `DATE`          | NOT NULL                                           |
| `base_target_savings` | `DECIMAL(19,2)` | NOT NULL (immutable original allocation)           |
| `target_savings`      | `DECIMAL(19,2)` | NOT NULL (mirrors base at creation)                |
| `actual_saved`        | `DECIMAL(19,2)` | NOT NULL, default `0`                              |
| `manually_completed`  | `BOOLEAN`       | NOT NULL, default `false`                          |
| `status`              | `VARCHAR(20)`   | NOT NULL (cached — do not trust; always recompute) |

---

## ⚙️ Environment Variables

| Variable              | Default                                              | Required in Prod               |
| --------------------- | ---------------------------------------------------- | ------------------------------ |
| `POSTGRES_URL`        | `jdbc:postgresql://localhost:5432/fintrack_planning` | ✅                             |
| `POSTGRES_USERNAME`   | `postgres`                                           | ✅                             |
| `POSTGRES_PASSWORD`   | `postgres`                                           | ✅                             |
| `EUREKA_HOST`         | `localhost`                                          | ✅                             |
| `EUREKA_USERNAME`     | `eureka`                                             | ✅                             |
| `EUREKA_PASSWORD`     | `eureka123`                                          | ✅                             |
| `FINTRACK_JWT_SECRET` | `fintrack-super-secret-key-change-me-in-prod-123`    | ✅ **Must match auth-service** |

---

## 🧪 Quick Test

```bash
# Login first and save token (see auth-service.md)
TOKEN="<your access token>"

# 1. Create a plan
curl -s -X POST http://localhost:8090/api/v1/plans \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "goalTitle": "Emergency Fund",
    "targetAmount": 6000.00,
    "timeframeCategory": "SHORT_TERM",
    "durationInMonths": 6,
    "frequency": "MONTHLY",
    "requiredPerPeriod": 1000.00,
    "startDate": "2026-07-01"
  }'

# 2. List plans
curl -s http://localhost:8090/api/v1/plans \
  -H "Authorization: Bearer $TOKEN"

# 3. Get plan detail (replace <planId>)
curl -s http://localhost:8090/api/v1/plans/<planId> \
  -H "Authorization: Bearer $TOKEN"

# 4. Update milestone actualSaved (replace <planId> and <milestoneId>)
curl -s -X PATCH http://localhost:8090/api/v1/plans/<planId>/milestones/<milestoneId> \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"actualSaved": 1000.00}'

# 5. Mark milestone complete
curl -s -X POST http://localhost:8090/api/v1/plans/<planId>/milestones/<milestoneId>/complete \
  -H "Authorization: Bearer $TOKEN"

# 6. Enable deficit redistribution
curl -s -X PATCH http://localhost:8090/api/v1/plans/<planId>/settings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"recalculateOnMissedDeadline": true}'

# 7. Delete the plan
curl -s -X DELETE http://localhost:8090/api/v1/plans/<planId> \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🌐 Swagger UI — How to Authenticate

1. Obtain an access token from `auth-service` (`POST /api/v1/auth/login`)
2. Open `http://localhost:8090/swagger-ui.html`
3. Click **Authorize 🔒** (top right)
4. Enter the token — **without** the `Bearer ` prefix
5. All subsequent requests automatically include the JWT header
