package com.fintrack.auth.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.security.core.GrantedAuthority;

/**
 * Application roles for RBAC (Role-Based Access Control).
 *
 * <p>Roles are stored as a {@code Set<Role>} in the {@code User} document.
 * Spring Security reads them as {@link GrantedAuthority} strings prefixed
 * with {@code "ROLE_"} (e.g. {@code "ROLE_ADMIN"}).
 *
 * <p>Serialized as lowercase strings in JSON (e.g. {@code "admin"}, {@code "user"})
 * so the Next.js BFF can store them directly in the roles cookie and the
 * middleware's {@code roles.includes("admin")} check works correctly.
 *
 * <p>Hierarchy (most privilege → least):
 * <ul>
 *   <li>{@code ADMIN} — full system access</li>
 *   <li>{@code USER}  — standard authenticated user access</li>
 * </ul>
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
public enum Role implements GrantedAuthority {

    /** Full administrative access. Assign by default administrators. */
    ADMIN,

    /** Standard user access — assigned by registration. */
    USER;

    /**
     * Returns the authority string used by Spring Security.
     * Format: {@code "ROLE_<name>"} (e.g. {@code "ROLE_ADMIN"}).
     */
    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    /** Serializes the role as a lowercase string for JSON output (e.g. {@code "admin"}). */
    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }

    /** Deserializes from either {@code "admin"} or {@code "ADMIN"} (case-insensitive). */
    @JsonCreator
    public static Role fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Role.valueOf(value.toUpperCase());
    }
}

