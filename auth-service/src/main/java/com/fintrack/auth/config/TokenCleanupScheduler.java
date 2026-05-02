package com.fintrack.auth.config;

import com.fintrack.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduled job to clean up expired refresh tokens from the database.
 *
 * <p>Unlike MongoDB's TTL index, PostgreSQL does not auto-expire rows.
 * This job runs daily (by default) to delete tokens whose
 * {@code expiry_date} has passed.
 *
 * <p>Enable scheduling in the main class with {@code @EnableScheduling}.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Runs every day at 02:00 AM to remove expired refresh tokens.
     *
     * <p>Cron expression: {@code "0 0 2 * * *"} — second, minute, hour, day, month, weekday.
     * Change {@code @Scheduled(cron = "...")} to adjust the schedule.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Running expired token cleanup at {}", now);

        refreshTokenRepository.deleteAllExpiredBefore(now);

        log.info("Expired refresh tokens cleanup completed");
    }
}

