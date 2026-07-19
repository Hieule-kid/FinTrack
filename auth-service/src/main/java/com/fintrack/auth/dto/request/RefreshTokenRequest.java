package com.fintrack.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for the {@code POST /api/v1/auth/refresh} endpoint.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Setter
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}

