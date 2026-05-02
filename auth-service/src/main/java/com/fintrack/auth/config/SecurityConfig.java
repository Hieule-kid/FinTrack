package com.fintrack.auth.config;

import com.fintrack.auth.filter.JwtAuthFilter;
import com.fintrack.auth.repository.UserRepository;
import com.fintrack.auth.service.JwtService;
import com.fintrack.core.exception.AppException;
import com.fintrack.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the Auth Service.
 *
 * <p>Security model:
 * <ul>
 *   <li>Stateless JWT — no HTTP session is created</li>
 *   <li>Public endpoints: {@code /api/v1/auth/**}, {@code /actuator/health}</li>
 *   <li>All other endpoints require a valid JWT</li>
 *   <li>{@code @PreAuthorize} is enabled via {@code @EnableMethodSecurity}</li>
 * </ul>
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Security Filter Chain
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Configures the HTTP security filter chain.
     *
     * @param http the {@link HttpSecurity} to configure
     * @param jwtAuthFilter the {@link JwtAuthFilter} bean
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            // Disable CSRF — not needed for stateless REST APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session — do NOT create or use HTTP sessions
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Request authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no authentication required
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/api/v1/auth/refresh",
                    "/actuator/health",
                    "/actuator/info",
                    // Swagger UI — allow access without JWT in dev
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml"
                ).permitAll()
                // All other requests require a valid JWT
                .anyRequest().authenticated()
            )

            // Wire authentication provider
            .authenticationProvider(authenticationProvider())

            // Add JWT filter before Spring's username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Authentication beans
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Loads user from MongoDB for Spring Security authentication.
     *
     * @return a {@link UserDetailsService} backed by MongoDB
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository
                .findByUsernameAndDeletedFalse(username)
                .or(() -> userRepository.findByEmailAndDeletedFalse(username))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * DAO authentication provider wiring the UserDetailsService and PasswordEncoder.
     *
     * @return configured {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * BCrypt password encoder with default strength (10 rounds).
     *
     * @return {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the {@link AuthenticationManager} as a Spring bean so it can be
     * injected into controllers or services if needed.
     *
     * @param config the Spring authentication configuration
     * @return the {@link AuthenticationManager}
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
    /**
     * Creates the JWT authentication filter bean.
     *
     * @return configured {@link JwtAuthFilter}
     */
    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        return new JwtAuthFilter(jwtService, userDetailsService);
    }
}

