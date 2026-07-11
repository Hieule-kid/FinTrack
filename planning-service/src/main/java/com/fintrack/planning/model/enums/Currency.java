package com.fintrack.planning.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Supported currencies for plan amounts.
 * Mirrors {@code com.fintrack.auth.model.enums.Currency} — kept local to avoid
 * cross-service coupling between planning-service and auth-service.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
public enum Currency {

    VND,
    USD;

    @JsonValue
    public String toValue() {
        return this.name();
    }

    @JsonCreator
    public static Currency fromValue(String value) {
        return Currency.valueOf(value.toUpperCase());
    }
}
