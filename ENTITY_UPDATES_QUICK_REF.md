# Entity Updates — Quick Reference

## 📊 Changes at a Glance

### Auth Service Entities

| Entity | Changes | Location |
|--------|---------|----------|
| **User** | ✅ @EqualsAndHashCode(of="id", callSuper=false)<br>✅ Added @NotBlank on username, email<br>✅ Added @NotNull on password<br>✅ Added @OneToMany accounts relationship | `auth-service/src/main/java/com/fintrack/auth/model/User.java` |
| **RefreshToken** | ✅ @EqualsAndHashCode(of="id")<br>✅ LocalDateTime → **Instant**<br>✅ Added @NotBlank on token, userId<br>✅ Added @NotNull on expiryDate | `auth-service/src/main/java/com/fintrack/auth/model/RefreshToken.java` |
| **Account (NEW)** | ✅ OAuth2 social login support<br>✅ ManyToOne FK to User<br>✅ Composite unique index<br>✅ All fields validated | `auth-service/src/main/java/com/fintrack/auth/model/Account.java` |

### Financial Service Entities (NEW)

| Entity | Features | Location |
|--------|----------|----------|
| **FinancialPlan** | ✅ Savings goal tracking<br>✅ **Soft link** to User (userId)<br>✅ OneToMany Milestones<br>✅ ZonedDateTime fields<br>✅ Progress calculation helpers | `financial-service/src/main/java/com/fintrack/financial/model/FinancialPlan.java` |
| **Milestone** | ✅ Sub-goals within plan<br>✅ Physical FK to FinancialPlan<br>✅ Completion status tracking<br>✅ ZonedDateTime fields<br>✅ Cascade delete on parent | `financial-service/src/main/java/com/fintrack/financial/model/Milestone.java` |

---

## 🔑 Key Improvements

### Validation Added ✅
```
User: username, email, password
RefreshToken: token, userId, expiryDate
Account: user, provider, providerAccountId, accessToken
FinancialPlan: userId, name, targetAmount (>0), targetDate
Milestone: financialPlan, name, targetAmount (>0), targetDate
```

### Equality & Hashing ✅
All entities use `@EqualsAndHashCode(of="id", callSuper=false)` for safe HashSet/HashMap usage

### Date Types ✅
- `Instant` for system-level UTC timestamps (RefreshToken.expiryDate)
- `ZonedDateTime` for user-facing deadlines (FinancialPlan, Milestone)
- `LocalDateTime` for internal audit (BaseEntity timestamps)

### Relationships ✅
- User ↔ Account: OneToMany, cascade delete
- FinancialPlan ↔ Milestone: OneToMany, orphan removal
- FinancialPlan → User: Soft link (userId string)

### Collections ✅
All relationship fields use `HashSet` with `@Builder.Default` initialization:
```java
@Builder.Default
private Set<Account> accounts = new HashSet<>();

@Builder.Default
private Set<Milestone> milestones = new HashSet<>();
```

---

## 📁 Files Created/Modified

### Created Files:
- ✨ `auth-service/src/main/java/com/fintrack/auth/model/Account.java`
- ✨ `financial-service/` — entire module
  - `pom.xml`
  - `Dockerfile`
  - `src/main/java/com/fintrack/financial/FinancialServiceApplication.java`
  - `src/main/java/com/fintrack/financial/model/FinancialPlan.java`
  - `src/main/java/com/fintrack/financial/model/Milestone.java`
  - `src/main/resources/application.yml`

### Modified Files:
- 📝 `auth-service/src/main/java/com/fintrack/auth/model/User.java`
- 📝 `auth-service/src/main/java/com/fintrack/auth/model/RefreshToken.java`
- 📝 `auth-service/src/main/java/com/fintrack/auth/service/impl/AuthServiceImpl.java`
  - Updated LocalDateTime → Instant for RefreshToken compatibility
- 📝 `pom.xml` — Added financial-service module

---

## 💻 Database Schema Overview

### Users & Auth
```sql
-- users (unchanged structure, enhanced lifecycle)
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    full_name VARCHAR(100),
    created_at, updated_at, created_by, updated_by,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- OAuth2 linked accounts (NEW)
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL FK,
    provider VARCHAR(50) NOT NULL,
    provider_account_id VARCHAR(255) NOT NULL,
    access_token TEXT NOT NULL,
    UNIQUE(user_id, provider, provider_account_id)
);

-- refresh_tokens (expiryDate changed to TIMESTAMP)
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(36) UNIQUE NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    expiry_date TIMESTAMP NOT NULL
);
```

### Financial Planning
```sql
-- Financial plans (NEW)
CREATE TABLE financial_plans (
    id UUID PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,  -- Soft link
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    target_amount NUMERIC(19,2) NOT NULL,
    current_savings NUMERIC(19,2) NOT NULL,
    target_date TIMESTAMP NOT NULL,
    created_at, updated_at, created_by, updated_by,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Milestones (NEW)
CREATE TABLE milestones (
    id UUID PRIMARY KEY,
    financial_plan_id UUID NOT NULL FK,
    name VARCHAR(200) NOT NULL,
    target_amount NUMERIC(19,2) NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    target_date TIMESTAMP NOT NULL,
    created_at, updated_at, created_by, updated_by,
    is_deleted BOOLEAN DEFAULT FALSE
);
```

---

## 🎯 Microservices Alignment

### Cross-Service Communication Pattern
```
Financial Service ←(readonly)→ Auth Service
    ↓ (via userId)
    [User UUID soft link]

Benefits:
- Each service owns its data
- No distributed transactions
- Services can be deployed independently
- Auth service outage doesn't break financial data query
```

### Query Pattern for Financial Plans
```java
// Always filter by userId for multi-tenancy
@Query("SELECT fp FROM FinancialPlan fp WHERE fp.userId = :userId AND fp.deleted = false")
List<FinancialPlan> findUserPlans(@Param("userId") String userId);
```

---

## ✅ Compilation Status

```
✓ FinTrack :: Parent
✓ FinTrack :: Config Service
✓ FinTrack :: Gateway Service
✓ FinTrack :: Core
✓ FinTrack :: Auth Service
✓ FinTrack :: Financial Service
✓ FinTrack :: Service Template

BUILD SUCCESS — No errors or warnings
```

---

## 🚀 Next Development Steps

1. **Repositories** — Create `FinancialPlanRepository`, `MilestoneRepository`
2. **Services** — Implement business logic for plan management
3. **DTOs** — Create request/response DTOs with validation
4. **Controllers** — Build REST API endpoints
5. **Mapping** — Add MapStruct mappers (Entity ↔ DTO)
6. **Tests** — Unit and integration test suites
7. **Gateway** — Add financial-service routes in gateway-service

---

**Alignment Checklist:**
- ✅ @EqualsAndHashCode on all entities
- ✅ Proper date types (Instant/ZonedDateTime)
- ✅ Validation annotations on essential fields
- ✅ Javadoc and code comments preserved
- ✅ Soft links properly documented
- ✅ Physical FKs with cascade delete where needed
- ✅ HashSet for one-to-many relationships
- ✅ Project compiles cleanly

---

*Last Updated: June 28, 2026*

