package com.fintrack.auth.service;

import com.fintrack.auth.dto.request.LoginRequest;
import com.fintrack.auth.dto.request.RefreshTokenRequest;
import com.fintrack.auth.dto.request.RegisterRequest;
import com.fintrack.auth.dto.response.AuthResponse;
import com.fintrack.auth.dto.response.UserResponse;

/**
 * Contract for authentication and user management operations.
 *
 * <p>Implementations handle credential validation, token issuance,
 * and user lifecycle management.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * Registers a new user account.
     *
     * @param request the validated registration payload
     * @return the created user's public profile
     * @throws com.fintrack.core.exception.AppException with {@code DUPLICATE_EMAIL} or
     *         {@code DUPLICATE_USERNAME} if the credential is already taken
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticates a user and issues JWT tokens.
     *
     * @param request the login credentials (identifier + password)
     * @return an {@link AuthResponse} containing access token, refresh token, and user profile
     * @throws com.fintrack.core.exception.AppException with {@code INVALID_CREDENTIALS}
     *         if authentication fails
     */
    AuthResponse login(LoginRequest request);

    /**
     * Issues a new access token using a valid refresh token.
     *
     * @param request the refresh token payload
     * @return a new {@link AuthResponse} with a fresh access token
     * @throws com.fintrack.core.exception.AppException with {@code INVALID_TOKEN}
     *         if the refresh token is expired, revoked, or not found
     */
    AuthResponse refresh(RefreshTokenRequest request);

    /**
     * Invalidates all refresh tokens for the given user (logs out all sessions).
     *
     * @param userId the ID of the user to log out
     */
    void logout(String userId);

    /**
     * Returns the public profile of a user by their ID.
     *
     * @param userId the user's MongoDB ObjectId
     * @return the user's public profile
     * @throws com.fintrack.core.exception.AppException with {@code USER_NOT_FOUND}
     *         if no user exists with this ID
     */
    UserResponse getProfile(String userId);
}

