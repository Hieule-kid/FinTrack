package com.fintrack.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity storing issued refresh tokens.
 *
 * <p>Refresh tokens are long-lived (default 7 days) and allow clients to obtain
 * new access tokens without re-authenticating. A token is invalidated on:
 * <ul>
 *   <li>Explicit logout ({@code deleteByUserId})</li>
 *   <li>Expiry ({@code expiryDate} is in the past — checked in the service layer)</li>
 *   <li>User account deletion (cascades via the service layer)</li>
 * </ul>
 *
 * <p>Table: {@code refresh_tokens}
 *
 * <p><b>Note:</b> Unlike MongoDB's TTL index, PostgreSQL does not auto-delete expired rows.
 * A scheduled cleanup job should periodically run:
 * {@code DELETE FROM refresh_tokens WHERE expiry_date < NOW()}
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
        @Index(name = "idx_refresh_tokens_token", columnList = "token", unique = true),
        @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")
    }
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    /** The opaque token string sent to and stored by the client. */
    @Column(name = "token", unique = true, nullable = false, length = 36)
    private String token;

    /** ID of the {@link User} this token belongs to. */
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    /**
     * Absolute expiry timestamp.
     * Tokens past this date are rejected and deleted lazily by the service layer.
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
}
