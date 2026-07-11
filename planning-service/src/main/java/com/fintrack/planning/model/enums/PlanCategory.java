package com.fintrack.planning.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Semantic category for a savings plan, matching the eight options
 * presented in the FE plan-creation form.
 *
 * <p>Serialized as lowercase strings (e.g. {@code "travel"}) for JSON
 * compatibility with the frontend.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
public enum PlanCategory {

    EMERGENCY,
    TRAVEL,
    HOUSE,
    CAR,
    EDUCATION,
    WEDDING,
    RETIREMENT,
    OTHER;

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }

    @JsonCreator
    public static PlanCategory fromValue(String value) {
        return PlanCategory.valueOf(value.toUpperCase());
    }
}
