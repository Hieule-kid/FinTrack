package com.fintrack.auth.model;


import com.fintrack.auth.model.enums.Currency;
import com.fintrack.auth.model.enums.Role;
import com.fintrack.core.base.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a FinTrack user account.
 *
 * <p>Implements {@link UserDetails} so Spring Security can load the user
 * directly via {@code UserDetailsService} without a separate adapter class.
 *
 * <p>Table: {@code users}
 * <p>Roles are stored in a join table {@code user_roles}.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

    /** Display name — not required to be unique. */
    @Column(name = "full_name", length = 100)
    private String fullName;

    /**
     * Username used for login — unique across the system.
     */
    @NotBlank(message = "Username cannot be blank")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    /**
     * Email address — unique, used as an alternative login identifier.
     */
    @NotBlank(message = "Email cannot be blank")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    /**
     * BCrypt-hashed password. Never store or return plain-text passwords.
     */
    @NotNull(message = "Password cannot be null")
    @Column(name = "password_hash", nullable = false)
    private String password;

    /**
     * Roles granted to this user.
     * Stored in a separate {@code user_roles} join table as VARCHAR strings.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    /**
     * OAuth2 linked accounts for social login (Google, GitHub, etc.).
     * Cascade delete ensures orphaned accounts are removed when the user is deleted.
     * Uses HashSet to prevent duplicate accounts.
     */
    @Builder.Default
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private Set<Account> accounts = new HashSet<>();

    // ─────────────────────────────────────────────────────────────────────────
    // UserDetails — Spring Security contract
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    /**
     * Delegates to {@link BaseEntity#isDeleted()} — a soft-deleted user cannot log in.
     */
    @Override
    public boolean isEnabled() {
        return !isDeleted();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }
}
