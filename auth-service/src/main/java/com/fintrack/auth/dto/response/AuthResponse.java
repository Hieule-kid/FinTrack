package com.fintrack.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response payload returned after a successful login or token refresh.
 *
 * <p>The client should:
 * <ol>
 *   <li>Store {@code accessToken} in memory (NOT localStorage)</li>
 *   <li>Store {@code refreshToken} in an HttpOnly cookie</li>
 *   <li>Attach {@code accessToken} as {@code Authorization: Bearer <token>} on every request</li>
 *   <li>Call {@code POST /api/v1/auth/refresh} when the access token expires</li>
 * </ol>
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    /** Short-lived JWT access token (default: 15 minutes). */
    private String accessToken;

    /**
     * Long-lived refresh token (default: 7 days).
     * Used to obtain new access tokens without re-login.
     */
    private String refreshToken;

    /** Token type — always {@code "Bearer"}. */
    @Builder.Default
    private String tokenType = "Bearer";

    /** Access token expiry in seconds (for client-side countdown). */
    private long expiresIn;

    /** Public profile of the authenticated user. */
    private UserResponse user;
}

