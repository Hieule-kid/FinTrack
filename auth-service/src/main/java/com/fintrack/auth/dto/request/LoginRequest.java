package com.fintrack.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for the {@code POST /api/v1/auth/login} endpoint.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Setter
public class LoginRequest {

    /**
     * The user's email address.
     * Can also accept a username — the service will try both.
     */
    @NotBlank(message = "Email or username is required")
    private String emailOrUsername;

    /** The plain-text password. Will be verified against the BCrypt hash. */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}

