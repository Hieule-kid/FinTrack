package com.fintrack.auth.controller;

import com.fintrack.auth.dto.request.LoginRequest;
import com.fintrack.auth.dto.request.RefreshTokenRequest;
import com.fintrack.auth.dto.request.RegisterRequest;
import com.fintrack.auth.dto.response.AuthResponse;
import com.fintrack.auth.dto.response.UserResponse;
import com.fintrack.auth.service.AuthService;
import com.fintrack.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 *
 * <p>Base path: {@code /api/v1/auth}
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, token refresh, and profile management")
public class AuthController {

    private final AuthService authService;

    // ─────────────────────────────────────────────────────────────────────────
    // Public endpoints
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
        summary = "Register a new user account",
        description = "Creates a new user with ROLE_USER. Returns the public profile (no tokens). Use `/login` to obtain JWT tokens."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed — check field errors in response body"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or username already in use")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "Registration successful"));
    }

    @Operation(
        summary = "Login and receive JWT tokens",
        description = "Authenticates with email/username + password. Returns an access token (15 min) and a refresh token (7 days)."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful — JWT tokens returned"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @Operation(
        summary = "Refresh access token",
        description = "Exchange a valid refresh token for a new access token. The old refresh token is revoked."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "New access token issued"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token is invalid or expired")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Protected endpoints — require valid JWT
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(
        summary = "Get current user profile",
        description = "Returns the authenticated user's public profile. Requires a valid Bearer JWT.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile returned"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        com.fintrack.auth.model.User user = (com.fintrack.auth.model.User) userDetails;
        UserResponse profile = authService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @Operation(
        summary = "Logout — revoke all sessions",
        description = "Invalidates all refresh tokens for the current user. Access tokens remain valid until they expire naturally.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Logged out successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserDetails userDetails) {
        com.fintrack.auth.model.User user = (com.fintrack.auth.model.User) userDetails;
        authService.logout(user.getId());
        return ResponseEntity.noContent().build();
    }
}

