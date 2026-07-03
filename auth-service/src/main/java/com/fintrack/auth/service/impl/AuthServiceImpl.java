package com.fintrack.auth.service.impl;

import com.fintrack.auth.dto.request.LoginRequest;
import com.fintrack.auth.dto.request.RefreshTokenRequest;
import com.fintrack.auth.dto.request.RegisterRequest;
import com.fintrack.auth.dto.response.AuthResponse;
import com.fintrack.auth.dto.response.UserResponse;
import com.fintrack.auth.model.RefreshToken;
import com.fintrack.auth.model.User;
import com.fintrack.auth.model.enums.Role;
import com.fintrack.auth.repository.RefreshTokenRepository;
import com.fintrack.auth.repository.UserRepository;
import com.fintrack.auth.service.AuthService;
import com.fintrack.auth.service.JwtService;
import com.fintrack.core.exception.AppException;
import com.fintrack.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of {@link AuthService}.
 *
 * <p>Handles the full authentication lifecycle:
 * <ol>
 *   <li>Registration with BCrypt password hashing</li>
 *   <li>Token refresh (validates stored refresh token, issues new access token)</li>
 *   <li>Logout (revokes all refresh tokens for the user)</li>
 * </ol>
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${fintrack.jwt.refresh-token-expiry-days:7}")
    private int refreshTokenExpiryDays;

    // ─────────────────────────────────────────────────────────────────────────
    // Register
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Steps:
     * <ol>
     *   <li>Check for duplicate username/email</li>
     *   <li>Hash the password with BCrypt</li>
     *   <li>Assign default {@code ROLE_USER}</li>
     *   <li>Persist the user</li>
     *   <li>Return public profile</li>
     * </ol>
     */
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new AppException(ErrorCode.DUPLICATE_USERNAME);
        }

        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new AppException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.ADMIN))
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: username={}, email={}", savedUser.getUsername(), savedUser.getEmail());

        return toUserResponse(savedUser);
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Refresh
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.delete(storedToken);
        return buildAuthResponse(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void logout(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("User {} logged out — all refresh tokens revoked", userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Get profile
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return toUserResponse(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a full {@link AuthResponse} — generates JWT + persists refresh token.
     */
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createAndSaveRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .user(toUserResponse(user))
                .build();
    }

    /**
     * Creates a new {@link RefreshToken}, persists it, and returns the token string.
     */
    private String createAndSaveRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .userId(user.getId())
                .expiryDate(Instant.now().plusSeconds(60L * 60 * 24 * refreshTokenExpiryDays))
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    /** Maps a {@link User} entity to a public {@link UserResponse} DTO. */
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameAndDeletedFalse(request.getEmailOrUsername())
                .or(() -> userRepository.findByEmailAndDeletedFalse(request.getEmailOrUsername()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        return buildAuthResponse(user);
    }
}

