package com.fintrack.auth.model.enums;

import org.springframework.security.core.GrantedAuthority;

/**
 * Application roles for RBAC (Role-Based Access Control).
 *
 * <p>Roles are stored as a {@code Set<Role>} in the {@code User} document.
 * Spring Security reads them as {@link GrantedAuthority} strings prefixed
 * with {@code "ROLE_"} (e.g. {@code "ROLE_ADMIN"}).
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

    /** Full administrative access. Assign only to system administrators. */
    ADMIN,

    /** Standard user access — assigned by default on registration. */
    USER;

    /**
     * Returns the authority string used by Spring Security.
     * Format: {@code "ROLE_<name>"} (e.g. {@code "ROLE_ADMIN"}).
     */
    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}

