package com.fintrack.core.exception;

import com.fintrack.core.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler — catches all exceptions thrown from controllers
 * and converts them to a consistent {@link ApiResponse} JSON structure.
 *
 * <p>This class is a shared library component. To activate it in a service,
 * ensure the service's main class scans the {@code com.fintrack} base package:
 * <pre>{@code
 * @SpringBootApplication(scanBasePackages = "com.fintrack")
 * }</pre>
 *
 * <p>Priority order (most specific → least specific):
 * <ol>
 *   <li>{@link AppException} — domain/business errors with {@link ErrorCode}</li>
 *   <li>{@link MethodArgumentNotValidException} — Bean Validation failures</li>
 *   <li>{@link Exception} — catch-all fallback (500 Internal Server Error)</li>
 * </ol>
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────────────────────────────────
    // AppException — business rule violations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles {@link AppException} thrown from the service layer.
     *
     * <p>Uses the {@link ErrorCode} to determine the HTTP status and response body.
     *
     * @param ex the caught application exception
     * @return a {@link ResponseEntity} with the mapped HTTP status and error body
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        // Log at WARN level — these are expected errors, not bugs
        log.warn("AppException [{}]: {}", errorCode.name(), ex.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode.getCode(), ex.getMessage()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bean Validation — @Valid / @Validated failures
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles validation errors from {@code @Valid} annotated request bodies.
     *
     * <p>Returns a map of {@code fieldName → errorMessage} in the data payload
     * so the frontend can highlight specific fields.
     *
     * @param ex the method argument validation exception
     * @return {@code 400 Bad Request} with field-level error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // Collect all field-level constraint violations; class-level constraints use objectName
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = (error instanceof FieldError fe) ? fe.getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed for {} field(s): {}", errors.size(), errors.keySet());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .code(ErrorCode.VALIDATION_ERROR.getCode())
                        .message(ErrorCode.VALIDATION_ERROR.getMessage())
                        .data(errors)
                        .build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Catch-all — unexpected errors
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fallback handler for all unhandled exceptions.
     *
     * <p>Logs the full stack trace at ERROR level and returns a generic
     * {@code 500 Internal Server Error} — stack traces are NEVER exposed to clients.
     *
     * @param ex the unexpected exception
     * @return {@code 500 Internal Server Error} with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        // Log the full stack trace — this is a bug, not a user error
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                ));
    }
}

