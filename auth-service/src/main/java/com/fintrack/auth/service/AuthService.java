package com.fintrack.auth.service;

import com.fintrack.auth.dto.request.LoginRequest;
import com.fintrack.auth.dto.request.RefreshTokenRequest;
import com.fintrack.auth.dto.request.RegisterRequest;
import com.fintrack.auth.dto.request.UpdateCurrencyRequest;
import com.fintrack.auth.dto.request.UpdateUserProfileRequest;
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

    /**
     * Updates the authenticated user's full name, email, and currency.
     *
     * @param userId  the ID of the user to update
     * @param request the validated profile payload
     * @return the updated user's public profile
     * @throws com.fintrack.core.exception.AppException with {@code USER_NOT_FOUND}
     *         if no user exists with this ID
     * @throws com.fintrack.core.exception.AppException with {@code DUPLICATE_EMAIL}
     *         if the email is already used by another user
     */
    UserResponse updateProfile(String userId, UpdateUserProfileRequest request);

    /**
     * Updates only the authenticated user's preferred currency.
     *
     * @param userId  the ID of the user to update
     * @param request the validated currency payload
     * @return the updated user's public profile
     * @throws com.fintrack.core.exception.AppException with {@code USER_NOT_FOUND}
     *         if no user exists with this ID
     */
    UserResponse updateCurrency(String userId, UpdateCurrencyRequest request);

    /**
     * Authenticates a user with email/username and password, then issues JWT tokens.
     *
     * <p>Accepts either a username or email address in the {@code emailOrUsername} field.
     * On success returns a new access token, a refresh token, and the user's public profile.
     *
     * @param request the validated login payload containing credentials
     * @return an {@link AuthResponse} containing the access token, refresh token,
     *         token type, expiry, and the user's public profile
     * @throws com.fintrack.core.exception.AppException with {@code USER_NOT_FOUND}
     *         if no user matches the given email/username
     * @throws com.fintrack.core.exception.AppException with {@code INVALID_CREDENTIALS}
     *         if the password does not match
     */
    AuthResponse login(LoginRequest request);
}

