package com.fintrack.core.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date/time operations used across FinTrack services.
 *
 * <p>This class is stateless — all methods are {@code static}.
 * Do not instantiate it.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
public final class DateUtils {

    /** ISO 8601 date format: {@code yyyy-MM-dd}. */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Standard display format: {@code dd/MM/yyyy HH:mm:ss}. */
    public static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /** Prevent instantiation. */
    private DateUtils() {
        throw new UnsupportedOperationException("DateUtils is a utility class");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Current time helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @return the current date-time in UTC
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }

    /**
     * @return the current date-time in Ho Chi Minh City timezone (UTC+7)
     */
    public static LocalDateTime nowVietnam() {
        return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Month range helpers (useful for budget/spending period queries)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the first moment of the given month ({@code yyyy-MM-01T00:00:00}).
     *
     * @param yearMonth the month to compute the start of
     * @return start of month as {@link LocalDateTime}
     */
    public static LocalDateTime startOfMonth(YearMonth yearMonth) {
        return yearMonth.atDay(1).atStartOfDay();
    }

    /**
     * Returns the last moment of the given month ({@code yyyy-MM-lastDayT23:59:59}).
     *
     * @param yearMonth the month to compute the end of
     * @return end of month as {@link LocalDateTime}
     */
    public static LocalDateTime endOfMonth(YearMonth yearMonth) {
        return yearMonth.atEndOfMonth().atTime(23, 59, 59);
    }

    /**
     * Returns the first moment of the current calendar month.
     *
     * @return start of the current month
     */
    public static LocalDateTime startOfCurrentMonth() {
        return startOfMonth(YearMonth.now());
    }

    /**
     * Returns the last moment of the current calendar month.
     *
     * @return end of the current month
     */
    public static LocalDateTime endOfCurrentMonth() {
        return endOfMonth(YearMonth.now());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Week range helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the Monday of the current ISO week.
     *
     * @return start of the current week (Monday 00:00:00)
     */
    public static LocalDateTime startOfCurrentWeek() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        return monday.atStartOfDay();
    }

    /**
     * Returns the Sunday of the current ISO week.
     *
     * @return end of the current week (Sunday 23:59:59)
     */
    public static LocalDateTime endOfCurrentWeek() {
        LocalDate sunday = LocalDate.now().with(DayOfWeek.SUNDAY);
        return sunday.atTime(23, 59, 59);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Formatting
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Formats a {@link LocalDateTime} to ISO date string {@code yyyy-MM-dd}.
     *
     * @param dateTime the date-time to format; must not be {@code null}
     * @return formatted date string
     */
    public static String toDateString(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * Parses an ISO date string ({@code yyyy-MM-dd}) to a {@link LocalDate}.
     *
     * @param dateStr the date string to parse; must not be {@code null}
     * @return parsed {@link LocalDate}
     */
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }
}

