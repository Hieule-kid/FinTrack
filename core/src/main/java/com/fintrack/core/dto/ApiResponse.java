package com.fintrack.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Unified API response envelope for all FinTrack REST endpoints.
 *
 * <p>Every endpoint returns this wrapper to ensure a consistent shape
 * that the frontend can rely on:
 * <pre>{@code
 * {
 *   "code":      200,
 *   "message":   "OK",
 *   "data":      { ... },
 *   "timestamp": "2026-04-26T10:00:00"
 * }
 * }</pre>
 *
 * <p>Fields with {@code null} values are omitted from the JSON output
 * ({@code @JsonInclude(NON_NULL)}).
 *
 * @param <T> the type of the {@code data} payload
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** HTTP status code mirrored into the response body for easy client access. */
    private int code;

    /** Human-readable status message. */
    private String message;

    /** The actual payload — {@code null} for empty responses (e.g. delete). */
    private T data;

    /** Server-side timestamp when this response was generated. */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ─────────────────────────────────────────────────────────────────────────
    // Factory helpers — preferred over calling the builder directly
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a {@code 200 OK} response with a data payload.
     *
     * @param data the response body; may be {@code null}
     * @param <T>  payload type
     * @return a success {@link ApiResponse}
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("OK")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a {@code 200 OK} response with a custom message.
     *
     * @param data    the response body
     * @param message a descriptive success message
     * @param <T>     payload type
     * @return a success {@link ApiResponse}
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a {@code 201 Created} response.
     *
     * @param data the created resource
     * @param <T>  payload type
     * @return a created {@link ApiResponse}
     */
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .code(201)
                .message("Created")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with a given HTTP status code and message.
     *
     * @param code    the HTTP error status code (4xx or 5xx)
     * @param message the error description
     * @return an error {@link ApiResponse} with no data payload
     */
    public static ApiResponse<Void> error(int code, String message) {
        return ApiResponse.<Void>builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

