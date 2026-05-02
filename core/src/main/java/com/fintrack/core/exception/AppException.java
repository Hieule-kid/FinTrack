package com.fintrack.core.exception;

import lombok.Getter;

/**
 * Base runtime exception for all FinTrack application errors.
 *
 * <p>Throw this exception (or a sub-class) from the service layer whenever
 * a business rule is violated. The {@link GlobalExceptionHandler} will
 * catch it and convert it to the appropriate HTTP response.
 *
 * <p>Usage:
 * <pre>{@code
 * // In service layer
 * User user = userRepository.findById(id)
 *     .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
 * }</pre>
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
public class AppException extends RuntimeException {

    /**
     * The error code that describes the failure.
     * Determines both the HTTP status and the response body {@code code} field.
     */
    private final ErrorCode errorCode;

    /**
     * Creates an {@link AppException} with the default message from the error code.
     *
     * @param errorCode the specific error; must not be {@code null}
     */
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * Creates an {@link AppException} with a custom detail message that overrides
     * the default message from the error code.
     *
     * @param errorCode the specific error; must not be {@code null}
     * @param message   a more detailed or contextual error description
     */
    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Creates an {@link AppException} wrapping an underlying cause.
     *
     * @param errorCode the specific error; must not be {@code null}
     * @param cause     the root cause
     */
    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}

