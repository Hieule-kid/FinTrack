# JPA Entity Updates — Microservices Architecture Alignment

**Date**: June 28, 2026  
**Project**: FinTrack — Personal Finance Management System  
**Scope**: Entity alignment with Microservices architecture best practices

---

## 📋 Summary of Changes

This document outlines all updates to JPA entities to align with microservices architecture, ensuring proper separation of concerns, validation, and relationship management.

---

## ✅ 1. Updated Entities in Auth Service (`auth-service`)

### 1.1 **User Entity** (`com.fintrack.auth.model.User`)

#### Changes Made:
- ✅ Added `@EqualsAndHashCode(of = "id", callSuper = false)` — ensures proper behavior in HashSets/Lists
- ✅ Added `@NotBlank` validation to `username` and `email` fields
- ✅ Added `@NotNull` validation to `password` field
- ✅ Added `@OneToMany` relationship to `Account` entities (orphan removal enabled)
- ✅ Uses `HashSet` for accounts to prevent duplicates

#### Key Features:
- Implements `UserDetails` — direct Spring Security integration
- Soft-delete support via `deleted` flag (inherited from `BaseEntity`)
- Role-based authorization via `@ElementCollection` join table
- Bidirectional relationship with OAuth2 accounts
- Audit fields (createdAt, updatedAt, createdBy, updatedBy) inherited from `BaseEntity`

#### Table: `users`

```sql
-- Primary entity for user accounts
CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_users_username ON users(username) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_email ON users(email) WHERE is_deleted = FALSE;
```

---

### 1.2 **RefreshToken Entity** (`com.fintrack.auth.model.RefreshToken`)

#### Changes Made:
- ✅ Added `@EqualsAndHashCode(of = "id")` — ensures proper equality checks
- ✅ Changed date type from `LocalDateTime` → `Instant` — better for UTC/global consistency
- ✅ Added `@NotBlank` validation to `token` and `userId` fields
- ✅ Added `@NotNull` validation to `expiryDate` field
- ✅ Updated javadoc to specify UTC timezone

#### Key Features:
- Long-lived token storage (default 7 days)
- UUID token generation — cryptographically secure
- Database indexes for O(1) lookups by token or user ID
- Soft link to User (no physical FK — handles cross-service deletions)
- Note: PostgreSQL does not auto-delete expired rows; scheduled cleanup job required

#### Table: `refresh_tokens`

```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(36) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
```

**Note**: Scheduled cleanup required:
```sql
-- Run daily at 02:00 AM via TokenCleanupScheduler
DELETE FROM refresh_tokens WHERE expiry_date < NOW();
```

---

### 1.3 **Account Entity (NEW)** — `com.fintrack.auth.model.Account`

#### Purpose:
Enables OAuth2 social login integration (Google, GitHub, Facebook, etc.).

#### Fields:
| Field | Type | Constraints | Purpose |
|-------|------|-----------|---------|
| `id` | UUID | PK, not updatable | Primary identifier |
| `user` | User | FK, @NotNull, @ManyToOne | Physical FK to User |
| `provider` | String(50) | @NotBlank | OAuth2 provider name (e.g., "google", "github") |
| `providerAccountId` | String(255) | @NotBlank | Provider-specific user ID (from OAuth2 token) |
| `accessToken` | TEXT | @NotNull | OAuth2 access token for API calls |

#### Key Features:
- Physical Foreign Key to User (cascade delete orphaned accounts)
- Composite unique index: `(user_id, provider, provider_account_id)`
- Prevents duplicate OAuth2 accounts per user per provider
- Uses HashSet internally to prevent duplicates

#### Table: `accounts`

```sql
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_account_id VARCHAR(255) NOT NULL,
    access_token TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_provider ON accounts(provider);
CREATE INDEX idx_accounts_provider_account_id ON accounts(provider_account_id);
CREATE UNIQUE INDEX idx_accounts_user_provider_unique 
    ON accounts(user_id, provider, provider_account_id);
```

#### Usage Example:
```java
// Link multiple OAuth2 providers to a single user
User user = userRepository.findById(userId).orElseThrow();

Account googleAccount = Account.builder()
    .user(user)
    .provider("google")
    .providerAccountId("google-user-123")
    .accessToken("google-access-token-xyz")
    .build();

Account githubAccount = Account.builder()
    .user(user)
    .provider("github")
    .providerAccountId("github-user-456")
    .accessToken("github-access-token-abc")
    .build();

user.getAccounts().add(googleAccount);
user.getAccounts().add(githubAccount);
userRepository.save(user);
```

---

## ✅ 2. New Financial Service (`financial-service`)

### Project Structure:
```
financial-service/
├── pom.xml
├── Dockerfile
├── src/main/
│   ├── java/com/fintrack/financial/
│   │   ├── FinancialServiceApplication.java
│   │   ├── model/
│   │   │   ├── FinancialPlan.java
│   │   │   └── Milestone.java
│   │   ├── repository/
│   │   ├── service/
│   │   ├── controller/
│   │   ├── dto/
│   │   └── config/
│   └── resources/
│       └── application.yml
└── src/test/
```

### 2.1 **FinancialPlan Entity** (`com.fintrack.financial.model.FinancialPlan`)

#### Purpose:
Represents a user's savings goal with a target amount and deadline.

#### Fields:
| Field | Type | Constraints | Purpose |
|-------|------|-----------|---------|
| `id` | UUID | PK | Inherited from BaseEntity |
| `userId` | String(36) | @NotNull, updatable=false | **Soft Link** to auth-service User |
| `name` | String(200) | @NotBlank, @NotNull | Goal name (e.g., "House Down Payment") |
| `description` | String(1000) | Optional | Goal context/purpose |
| `targetAmount` | BigDecimal | @NotNull, @Min(1) | Target savings amount (must be > 0) |
| `currentSavings` | BigDecimal | @NotNull, default=0 | Accumulated progress toward goal |
| `targetDate` | ZonedDateTime | @NotNull | Deadline (UTC timezone) |
| `milestones` | Set<Milestone> | @OneToMany, orphanRemoval | Sub-goals tracking |

#### Key Features:
- Extends `BaseEntity` — inherits id, timestamps, soft-delete, audit fields
- **Soft Link** to User via `userId` VARCHAR UUID (no physical FK across services)
- Composed of multiple milestones to break down goals into achievable chunks
- Helper methods: `calculateProgressPercentage()`, `isCompleted()`
- Uses `HashSet<Milestone>` to prevent duplicate milestones
- @EqualsAndHashCode(of = "id", callSuper = false) for proper Set behavior

#### Table: `financial_plans`

```sql
CREATE TABLE financial_plans (
    id UUID PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    target_amount NUMERIC(19,2) NOT NULL,
    current_savings NUMERIC(19,2) NOT NULL DEFAULT 0,
    target_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_financial_plans_user_id ON financial_plans(user_id) 
    WHERE is_deleted = FALSE;
CREATE INDEX idx_financial_plans_target_date ON financial_plans(target_date);
```

#### Usage Example:
```java
FinancialPlan plan = FinancialPlan.builder()
    .userId("user-uuid-123")  // Soft link to auth-service User
    .name("House Down Payment")
    .description("Save for home purchase")
    .targetAmount(new BigDecimal("50000.00"))
    .currentSavings(new BigDecimal("10000.00"))
    .targetDate(ZonedDateTime.now().plusYears(3))
    .build();

// Add milestones
Milestone m1 = Milestone.builder()
    .financialPlan(plan)
    .name("First Quarter Savings")
    .targetAmount(new BigDecimal("5000.00"))
    .targetDate(ZonedDateTime.now().plusMonths(3))
    .build();

plan.getMilestones().add(m1);

// Helper methods
BigDecimal progress = plan.calculateProgressPercentage();  // 20%
boolean complete = plan.isCompleted();  // false
```

---

### 2.2 **Milestone Entity** (`com.fintrack.financial.model.Milestone`)

#### Purpose:
Represents a sub-goal or checkpoint within a FinancialPlan.

#### Fields:
| Field | Type | Constraints | Purpose |
|-------|------|-----------|---------|
| `id` | UUID | PK | Inherited from BaseEntity |
| `financialPlan` | FinancialPlan | @NotNull, @ManyToOne, FK | Parent plan |
| `name` | String(200) | @NotBlank, @NotNull | Milestone name (e.g., "Initial Deposit") |
| `targetAmount` | BigDecimal | @NotNull, @Min(1) | Goal amount for this milestone |
| `completed` | boolean | default=false | Completion status |
| `targetDate` | ZonedDateTime | @NotNull | Deadline (UTC timezone) |
| (inherited) | | | createdAt, updatedAt, deleted, etc. |

#### Key Features:
- Extends `BaseEntity` — inherits audit fields
- Physical Foreign Key to FinancialPlan (cascade delete on parent delete)
- Milestone is "completed" when target is reached or marked done
- Dates stored as `ZonedDateTime` for global timezone support
- @EqualsAndHashCode(of = "id", callSuper = false) for Set behavior

#### Table: `milestones`

```sql
CREATE TABLE milestones (
    id UUID PRIMARY KEY,
    financial_plan_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    target_amount NUMERIC(19,2) NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    target_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (financial_plan_id) REFERENCES financial_plans(id) ON DELETE CASCADE
);

CREATE INDEX idx_milestones_financial_plan_id ON milestones(financial_plan_id) 
    WHERE is_deleted = FALSE;
CREATE INDEX idx_milestones_target_date ON milestones(target_date);
```

#### Usage Example:
```java
Milestone m = Milestone.builder()
    .financialPlan(plan)
    .name("First $5,000 Saved")
    .targetAmount(new BigDecimal("5000.00"))
    .targetDate(ZonedDateTime.of(2026, 9, 28, 0, 0, 0, 
        ZoneId.of("America/New_York")))
    .completed(false)
    .build();
```

---

## 🎯 Validation Annotations Summary

All essential fields now include validation:

| Entity | Field | Annotations | Message |
|--------|-------|-------------|---------|
| **User** | username | @NotBlank | "Username cannot be blank" |
| | email | @NotBlank | "Email cannot be blank" |
| | password | @NotNull | "Password cannot be null" |
| **RefreshToken** | token | @NotBlank | "Token cannot be blank" |
| | userId | @NotBlank | "User ID cannot be blank" |
| | expiryDate | @NotNull | "Expiry date cannot be null" |
| **Account** | user | @NotNull | "User cannot be null" |
| | provider | @NotBlank | "Provider cannot be blank" |
| | providerAccountId | @NotBlank | "Provider account ID cannot be blank" |
| | accessToken | @NotNull | "Access token cannot be null" |
| **FinancialPlan** | userId | @NotNull | "User ID cannot be null" |
| | name | @NotBlank | "Plan name cannot be blank" |
| | targetAmount | @NotNull, @Min(1) | "Target amount must be greater than 0" |
| | targetDate | @NotNull | "Target date cannot be null" |
| **Milestone** | financialPlan | @NotNull | "Financial plan cannot be null" |
| | name | @NotBlank | "Milestone name cannot be blank" |
| | targetAmount | @NotNull, @Min(1) | "Target amount must be greater than 0" |
| | targetDate | @NotNull | "Target date cannot be null" |

---

## 🌐 Soft Links vs Physical Foreign Keys

### Auth Service Entities (Physical FKs):
- `Account` → `User`: Physical FK (cascade delete)
- `RefreshToken` → `User`: Soft link (string userId)

### FinancialPlan Entity (Soft Link):
- `FinancialPlan.userId` → Auth Service User: **Soft Link** (no physical FK)
  - Reason: Data owned by different services
  - Multi-tenancy: Filter queries by userId
  - Resilience: Financial data survives user account deletion

### FinancialPlan to Milestone (Physical FK):
- `Milestone` → `FinancialPlan`: Physical FK (cascade delete)
  - Reason: Same service ownership
  - Lifecycle: Delete plan → auto-delete milestones

---

## 📅 Date/Time Fields

All temporal fields now use appropriate Java 8+ time API:

| Entity | Field | Type | Reason |
|--------|-------|------|--------|
| BaseEntity | createdAt, updatedAt | LocalDateTime | Database-level default (UTC) |
| RefreshToken | expiryDate | **Instant** | UTC-aware, global consistency |
| FinancialPlan | targetDate | **ZonedDateTime** | User-facing deadline, timezone-aware |
| Milestone | targetDate | **ZonedDateTime** | User-facing deadline, timezone-aware |

**Why ZonedDateTime for financial plans?**
- Users expect deadlines in their local timezone
- Example: "Save by Dec 31, 2026 11:59 PM EST"
- ZonedDateTime preserves timezone information when serialized

**Why Instant for token expiry?**
- Tokens are system-level, internal mechanics
- Instant represents absolute point in time (UTC)
- Better for comparisons and cross-timezone operations

---

## 🔧 Lombok Best Practices

All entities use consistent Lombok pattern:

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "...")
public class YourEntity extends BaseEntity {
    // ...
}
```

**Key points:**
- `@EqualsAndHashCode(of = "id", callSuper = false)`: 
  - Uses only `id` for equality — prevents issues with lazy-loading relationships
  - `callSuper = false` — avoids calling BaseEntity's equals (which includes `deleted` flag)
  - Safe for HashSet/HashMap operations

---

## 📦 New Module Registration

Added to `/pom.xml`:
```xml
<modules>
    <module>config-service</module>
    <module>gateway-service</module>
    <module>core</module>
    <module>auth-service</module>
    <module>financial-service</module>  <!-- NEW -->
    <module>service-template</module>
</modules>
```

---

## 🚀 Setup Instructions

### 1. Create Financial Database
```bash
docker exec -it fintrack-postgres psql -U postgres -c "CREATE DATABASE fintrack_financial;"
```

### 2. Build and Package
```bash
cd /path/to/fintrack
./mvnw clean install -DskipTests
```

### 3. Start Services (in order)
```bash
# Terminal 1: Config Service (Eureka)
cd config-service && ../mvnw spring-boot:run

# Terminal 2: Auth Service
cd auth-service && ../mvnw spring-boot:run

# Terminal 3: Financial Service
cd financial-service && ../mvnw spring-boot:run
```

### 4. Verify
```bash
# Check Eureka Dashboard
curl http://eureka:eureka123@localhost:8761/eureka/apps

# Financial Service Health
curl http://localhost:8083/actuator/health
```

---

## ✨ Compilation Verification

All modules compile successfully:
```
[INFO] FinTrack :: Parent ................................. SUCCESS
[INFO] FinTrack :: Config Service ......................... SUCCESS
[INFO] FinTrack :: Gateway Service ........................ SUCCESS
[INFO] FinTrack :: Core ................................... SUCCESS
[INFO] FinTrack :: Auth Service ........................... SUCCESS
[INFO] FinTrack :: Financial Service ...................... SUCCESS
[INFO] FinTrack :: Service Template ....................... SUCCESS
[INFO] BUILD SUCCESS
```

---

## 📝 Next Steps

1. **Create Repositories**: Implement `FinancialPlanRepository` and `MilestoneRepository`
2. **Add Services**: Business logic for plan creation, milestone tracking, progress calculation
3. **Add DTOs**: Request/Response DTOs for API endpoints
4. **Add Controllers**: REST endpoints for financial planning
5. **Add Tests**: Unit and integration tests for all entities
6. **Update Gateway Routes**: Add routes for financial-service in gateway-service/application.yml

---

**Document Version**: 1.0  
**Last Updated**: June 28, 2026  
**Author**: FinTrack Development Team

