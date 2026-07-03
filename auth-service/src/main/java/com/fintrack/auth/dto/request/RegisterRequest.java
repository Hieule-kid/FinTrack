package com.fintrack.auth.dto.request;

import com.fintrack.auth.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for the {@code POST /api/v1/auth/register} endpoint.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Setter
public class RegisterRequest {

    /** Full display name of the user. */
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    /**
     * Unique username — alphanumeric and underscores only.
     * 3–30 characters.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    /** Valid email address — used as an alternative login identifier. */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Plain-text password.
     * Must be at least 8 characters and contain at least one digit.
     * Will be hashed with BCrypt before storage.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one digit")
    private String password;

    private Role role;
}

