package com.fintrack.auth.repository;

import com.fintrack.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for {@link User} entities.
 *
 * <p>Spring Data JPA generates implementations at runtime.
 * All queries automatically filter soft-deleted users via {@code deleted = false}.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Finds a non-deleted user by their username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByUsernameAndDeletedFalse(String username);

    /**
     * Finds a non-deleted user by their email address.
     *
     * @param email the email to search for
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByEmailAndDeletedFalse(String email);

    /**
     * Checks whether a username is already taken by a non-deleted user.
     *
     * @param username the username to check
     * @return {@code true} if the username is already in use
     */
    boolean existsByUsernameAndDeletedFalse(String username);

    /**
     * Checks whether an email is already registered by a non-deleted user.
     *
     * @param email the email address to check
     * @return {@code true} if the email is already in use
     */
    boolean existsByEmailAndDeletedFalse(String email);
}
