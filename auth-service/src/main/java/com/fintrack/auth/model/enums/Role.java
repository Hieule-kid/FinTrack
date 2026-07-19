package com.fintrack.auth.model.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    /** Full administrative access. Assign by default administrators. */
    ADMIN,

    /** Standard user access — assigned by registration. */
    USER;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}

