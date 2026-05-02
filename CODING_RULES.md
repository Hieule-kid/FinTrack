# FinTrack — Java Spring Boot Coding Rules

> Best-practice conventions for all FinTrack backend services.  
> **Every developer must read this before their first commit.**

---

## 1. 📦 Package Structure

Every service must follow this layered layout:

```
com.fintrack.<service>/
├── <ServiceName>Application.java   # Main class only — no logic
├── config/                         # Spring @Configuration beans + OpenApiConfig
├── controller/                     # @RestController — HTTP layer ONLY
├── service/                        # Business logic interfaces
│   └── impl/                       # Business logic implementations
├── repository/                     # Spring Data JPA repositories
├── model/                          # JPA @Entity classes
│   └── enums/                      # Enums used by entities
├── dto/
│   ├── request/                    # Inbound DTOs (annotated with @Valid constraints)
│   └── response/                   # Outbound DTOs (safe public view — no sensitive fields)
├── filter/                         # Servlet filters (e.g. JwtAuthFilter)
├── exception/                      # Service-specific exceptions (if any beyond AppException)
└── mapper/                         # MapStruct mappers (DTO ↔ Entity)
```

**Hard rules:**
- ❌ Never put business logic in controllers
- ❌ Never return `@Entity` classes from controllers — always use response DTOs
- ❌ Never use `@Autowired` on fields — always use constructor injection (`@RequiredArgsConstructor`)
- ❌ Never access the repository directly from the controller

---

## 2. 📝 Javadoc Comments

### Class-level (required on all public classes)

```java
/**
 * One-line summary ending with a period.
 *
 * <p>Longer description. Explain the PURPOSE, not the implementation.
 * Use {@code <p>} tags for new paragraphs.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * TransactionService svc = context.getBean(TransactionService.class);
 * TransactionResponse res = svc.create(request);
 * }</pre>
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
```

### Method-level (required on all public methods)

```java
/**
 * One-line summary.
 *
 * <p>Optional extra explanation — what, not how.
 *
 * @param userId  the UUID of the target user; must not be {@code null}
 * @param request the validated update payload
 * @return        the updated resource as a response DTO
 * @throws AppException with {@link ErrorCode#RESOURCE_NOT_FOUND} if not found
 */
public TransactionResponse update(String userId, UpdateTransactionRequest request) {
```

### Inline comments

```java
// ── Section separator ────────────────────────────────────────────────────

// Explain WHY, not WHAT.
// ❌ BAD:  // set the amount field
// ✅ GOOD: // Round to 2 decimal places to match bank statement precision

/* Never use block comments inside methods */
```

---

## 3. 🏗️ Naming Conventions

| Type                | Pattern                 | Example                          |
|---------------------|-------------------------|----------------------------------|
| Entity              | `<Domain>`              | `Transaction`, `Budget`          |
| Repository          | `<Domain>Repository`    | `TransactionRepository`          |
| Service interface   | `<Domain>Service`       | `TransactionService`             |
| Service impl        | `<Domain>ServiceImpl`   | `TransactionServiceImpl`         |
| Controller          | `<Domain>Controller`    | `TransactionController`          |
| Create DTO          | `Create<Domain>Request` | `CreateTransactionRequest`       |
| Update DTO          | `Update<Domain>Request` | `UpdateTransactionRequest`       |
| Response DTO        | `<Domain>Response`      | `TransactionResponse`            |
| MapStruct mapper    | `<Domain>Mapper`        | `TransactionMapper`              |
| Enum                | Singular noun           | `Role`, `TransactionType`        |
| Config class        | `<Domain>Config`        | `SecurityConfig`, `OpenApiConfig`|
| Scheduler           | `<Domain>Scheduler`     | `TokenCleanupScheduler`          |

---

## 4. 🔤 Field & Method Naming

```java
// Fields: camelCase
private String transactionId;
private BigDecimal amountInVnd;   // ✅ always BigDecimal for money — NEVER float/double
private LocalDateTime createdAt;  // ✅ always LocalDateTime — NEVER java.util.Date

// Constants: UPPER_SNAKE_CASE
public static final String BEARER_PREFIX       = "Bearer ";
public static final int    MAX_LOGIN_ATTEMPTS  = 5;

// Boolean: prefix with is/has/can
private boolean deleted;   // getter → isDeleted()
private boolean active;    // getter → isActive()

// Methods: verb + noun
TransactionResponse createTransaction(...)   // ✅
TransactionResponse getTransaction(...)      // ✅
void deleteTransaction(...)                  // ✅
boolean isTokenExpired(...)                  // ✅ predicate
```

---

## 5. 🎯 Controller Rules

```java
@RestController
@RequestMapping("/api/v1/transactions")   // ✅ Always version the API (/api/v1/...)
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Create, read, update, delete transactions")
@SecurityRequirement(name = "Bearer Authentication")   // ✅ Always declare Swagger security
public class TransactionController {

    private final TransactionService transactionService;   // ✅ constructor injection

    @Operation(summary = "Create a new transaction")
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(
            @Valid @RequestBody CreateTransactionRequest request) {   // ✅ @Valid always

        // ✅ Delegate ALL logic — zero business code in controllers
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(transactionService.create(request), "Created"));
    }
}
```

---

## 6. 🔧 Service Rules

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;

    @Override
    @Transactional          // ✅ Required when multiple DB writes in one method
    public TransactionResponse create(CreateTransactionRequest request) {
        // Step 1: Validate business rules (throw AppException on failure)
        // Step 2: Map DTO → Entity
        // Step 3: repository.save(entity)
        // Step 4: Map Entity → ResponseDTO
        // Step 5: return DTO — NEVER return the entity

        // ✅ Log levels:
        log.debug("Detailed data: field={}", value);         // dev-only verbose
        log.info("Transaction created: id={}", saved.getId()); // important event
        log.warn("Duplicate attempt: userId={}", userId);    // expected bad input
        log.error("Payment failed: {}", ex.getMessage(), ex); // always include ex object
    }
}
```

---

## 7. 💾 Repository Rules (Spring Data JPA)

```java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    // ✅ Always filter soft-deleted rows in method name
    Page<Transaction> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    // ✅ Use @Query for complex SQL — always add a comment explaining the intent
    // Finds all transactions within a date range for monthly report generation
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
           "AND t.transactionDate BETWEEN :from AND :to AND t.deleted = false")
    List<Transaction> findByUserAndDateRange(
        @Param("userId") String userId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    // ✅ Bulk deletes require @Modifying + @Transactional
    @Modifying
    @Transactional
    @Query("DELETE FROM Transaction t WHERE t.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);

    // ❌ Never return deleted rows without explicit intent
    // ❌ Never use raw queries without an explaining comment
}
```

---

## 8. 🗄️ Entity Rules (JPA + PostgreSQL)

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "transactions",    // ✅ snake_case, plural table name
    indexes = {
        @Index(name = "idx_transactions_user_id", columnList = "user_id"),
        @Index(name = "idx_transactions_date",    columnList = "transaction_date")
    }
)
public class Transaction extends BaseEntity {   // ✅ Always extend BaseEntity

    // ✅ Use @Column with name= (snake_case), nullable=, length= for every field
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    // ✅ BigDecimal for money — set precision and scale explicitly
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    // ✅ LocalDateTime for timestamps
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    // ✅ Enums stored as STRING (readable SQL, survives enum reordering)
    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    // ✅ Text fields: always set length (default 255 may be too short or wasteful)
    @Column(name = "note", length = 500)
    private String note;

    // ❌ No business logic in entities
    // ❌ No @Transient for computed values — use the response DTO instead
}
```

---

## 9. 🔍 Swagger / OpenAPI Rules

```java
// ✅ Every controller must have @Tag
@Tag(name = "Transactions", description = "Manage user financial transactions")

// ✅ Every public endpoint must have @Operation
@Operation(
    summary = "One-line summary — shown in the endpoint list",
    description = "Longer explanation for the Swagger UI detail panel"
)

// ✅ Document possible HTTP responses
@io.swagger.v3.oas.annotations.responses.ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "400", description = "Validation error"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Not found")
})

// ✅ Protected endpoints must declare security requirement
@SecurityRequirement(name = "Bearer Authentication")

// ✅ Use @Schema to document DTO fields in Swagger
public class CreateTransactionRequest {
    @Schema(description = "Transaction amount in VND", example = "150000.00")
    @NotNull
    private BigDecimal amount;
}

// ❌ Do NOT add Swagger annotations to the core library (import conflict)
// ❌ Do NOT expose /v3/api-docs or /swagger-ui in production without auth
```

---

## 10. 🛡️ Security Rules

- ❌ **NEVER log passwords** — not even hashed ones
- ❌ **NEVER return** the `password` field in any response DTO
- ❌ **NEVER store** plain-text passwords — always BCrypt
- ❌ **NEVER catch and swallow** `AppException` — let `GlobalExceptionHandler` handle it
- ✅ **Always validate** JWT in `JwtAuthFilter` — not in controller or service
- ✅ **Always use** `@PreAuthorize("hasRole('ADMIN')")` on admin-only endpoints
- ✅ **Always use** HTTPS in production

```java
// ✅ Correct pattern — throw, let the handler respond
User user = userRepository.findById(id)
    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

// ❌ Wrong — hiding errors
try {
    User user = userRepository.findById(id).orElseThrow(...);
} catch (Exception e) {
    return null;   // swallowing is FORBIDDEN
}
```

---

## 11. ⚙️ Configuration Rules

```yaml
spring:
  datasource:
    # ✅ Always use ${ENV_VAR:default} — never hardcode credentials
    url:      ${POSTGRES_URL:jdbc:postgresql://localhost:5432/fintrack_dev}
    username: ${POSTGRES_USERNAME:postgres}
    password: ${POSTGRES_PASSWORD:postgres}
  jpa:
    hibernate:
      # ✅ Comment the allowed values and production recommendation
      # dev: "update" | test: "create-drop" | prod: "validate"
      ddl-auto: ${JPA_DDL_AUTO:update}

fintrack:
  jwt:
    # ✅ Warn that the dev default must be replaced in production
    # Minimum 32 chars for HS256. Use a secrets manager in production.
    secret: ${FINTRACK_JWT_SECRET:dev-only-change-in-production}
```

- ❌ Never hardcode secrets, passwords, or API keys
- ✅ Always provide `${VAR:default}` placeholders
- ✅ Use **`validate`** (not `update`) for `ddl-auto` in production
- ✅ Use **Flyway or Liquibase** for production schema migrations

---

## 12. 📊 Logging Rules

```java
@Slf4j   // ✅ Always use Lombok @Slf4j — never System.out.println
public class TransactionServiceImpl {

    public void process(String userId, BigDecimal amount) {
        log.debug("Processing: userId={}, amount={}", userId, amount);     // dev verbose
        log.info("Payment processed: txId={}, userId={}", id, userId);     // significant
        log.warn("Low balance warning: userId={}, balance={}", userId, b); // expected issue
        log.error("Payment failed for userId={}: {}", userId, ex.getMessage(), ex); // include ex!
    }
}

// ❌ Never do these:
log.info("Password hash: " + user.getPassword());   // sensitive data leak
log.error(ex.getMessage());                          // stack trace missing
System.out.println("debug: " + value);               // not a logger
"Processing " + userId + " amount " + amount         // string concat kills performance
```

---

## 13. ✅ Pull Request Checklist

Before opening a PR, verify every item:

**Code Quality**
- [ ] All public classes and methods have Javadoc (`@author`, `@since`, `@param`, `@return`)
- [ ] No business logic in controllers
- [ ] No `@Entity` returned from any endpoint — only response DTOs

**Database**
- [ ] Every `@Entity` extends `BaseEntity`
- [ ] All `@Column` names use `snake_case`
- [ ] `BigDecimal` used for monetary values (never `float`/`double`)
- [ ] Soft-delete filter (`deletedFalse`) applied in all repository queries
- [ ] `@Transactional` added to multi-step write operations
- [ ] `@Modifying` + `@Transactional` added to bulk delete / update queries

**Security**
- [ ] No plain-text secrets in code or `application.yml`
- [ ] No `password` field in any response DTO
- [ ] All protected endpoints have `@PreAuthorize` or security filter coverage

**Swagger**
- [ ] Controller has `@Tag`
- [ ] Each endpoint has `@Operation(summary = ...)`
- [ ] Protected endpoints declare `@SecurityRequirement(name = "Bearer Authentication")`

**General**
- [ ] `@Valid` on all `@RequestBody` parameters
- [ ] `AppException` + `ErrorCode` used for all errors (no raw `RuntimeException`)
- [ ] Logging uses parameterised messages — no string concatenation
- [ ] No `TODO` without a linked GitHub issue number

---

*FinTrack Coding Standards — v1.1 — April 2026*
