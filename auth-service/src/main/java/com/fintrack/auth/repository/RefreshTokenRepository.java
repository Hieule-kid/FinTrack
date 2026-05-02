package com.fintrack.auth.repository;

import com.fintrack.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JPA repository for {@link RefreshToken} entities.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    /**
     * Finds a refresh token document by its token string.
     *
     * @param token the token value to look up
     * @return an {@link Optional} containing the token if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes all refresh tokens belonging to a specific user.
     * Called on logout to invalidate all active sessions.
     *
     * <p>{@code @Modifying} + {@code @Transactional} are required for
     * bulk delete operations in Spring Data JPA.
     *
     * @param userId the user's ID
     */
    @Modifying
    @Transactional
    void deleteByUserId(String userId);

    /**
     * Scheduled cleanup: deletes all expired tokens.
     *
     * <p>Call this periodically (e.g. with {@code @Scheduled}) since PostgreSQL
     * does not have a MongoDB-style TTL index for automatic expiry.
     *
     * @param now the current timestamp — all tokens with {@code expiryDate < now} are deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteAllExpiredBefore(LocalDateTime now);
}
