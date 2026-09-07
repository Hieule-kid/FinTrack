package com.fintrack.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enumeration of all application error codes used across FinTrack services.
 *
 * <p>Each constant pairs an HTTP status with a machine-readable code and a
 * human-readable message. Using an enum ensures:
 * <ul>
 *   <li>All error codes are centralised and easy to find</li>
 *   <li>Error messages are consistent across services</li>
 *   <li>Frontend can map {@code code} values to localised strings</li>
 * </ul>
 *
 * <p>Convention:
 * <ul>
 *   <li>{@code 1xxx} — Authentication / Authorisation errors</li>
 *   <li>{@code 2xxx} — Resource not found errors</li>
 *   <li>{@code 3xxx} — Validation / Business rule errors</li>
 *   <li>{@code 4xxx} — Planning / budgeting domain errors</li>
 *   <li>{@code 5xxx} — Server / infrastructure errors</li>
 * </ul>
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
public enum ErrorCode {

    // ─── 1xxx — Authentication / Authorisation ───────────────────────────────
    UNAUTHORIZED(1001, "Authentication required", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1002, "Access denied", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1003, "JWT token is invalid or expired", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(1004, "Invalid email or password", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1005, "Token has expired", HttpStatus.UNAUTHORIZED),

    // ─── 2xxx — Resource not found ────────────────────────────────────────────
    RESOURCE_NOT_FOUND(2001, "Resource not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(2002, "User not found", HttpStatus.NOT_FOUND),

    // ─── 3xxx — Validation / Business rules ──────────────────────────────────
    VALIDATION_ERROR(3001, "Validation failed", HttpStatus.BAD_REQUEST),
    DUPLICATE_EMAIL(3002, "Email address is already in use", HttpStatus.CONFLICT),
    DUPLICATE_USERNAME(3003, "Username is already taken", HttpStatus.CONFLICT),
    INVALID_REQUEST(3004, "Invalid request parameters", HttpStatus.BAD_REQUEST),

    // ─── 4xxx — Planning / Budgeting domain ─────────────────────────────────
    PLAN_NOT_FOUND(4001, "Plan not found", HttpStatus.NOT_FOUND),
    MILESTONE_NOT_FOUND(4002, "Milestone not found", HttpStatus.NOT_FOUND),
    DUPLICATE_CATEGORY_NAME(4010, "An expense category with this name already exists", HttpStatus.CONFLICT),
    EXPENSE_NOT_FOUND(4011, "Expense not found", HttpStatus.NOT_FOUND),
    EXPENSE_CATEGORY_NOT_FOUND(4012, "Expense category not found", HttpStatus.NOT_FOUND),
    EXPENSE_PLAN_MISMATCH(4013, "The linked plan does not belong to the current user", HttpStatus.FORBIDDEN),
    INVALID_DATE_RANGE(4020, "The end date must not be before the start date", HttpStatus.BAD_REQUEST),
    DATE_RANGE_TOO_LARGE(4021, "The date range must not exceed 31 days", HttpStatus.BAD_REQUEST),
    PARTIAL_DATE_RANGE(4022, "Provide both 'from' and 'to' dates, or neither", HttpStatus.BAD_REQUEST),

    // ─── 5xxx — Server / Infrastructure ─────────────────────────────────────
    INTERNAL_SERVER_ERROR(5001, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(5002, "Service is temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE);

    /** Numeric code included in the response body — used by the frontend for i18n. */
    private final int code;

    /** Default English message; frontend should prefer its own localised strings. */
    private final String message;

    /** The HTTP status code to return when this error is thrown. */
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
