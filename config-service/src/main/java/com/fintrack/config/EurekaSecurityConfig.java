package com.fintrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Eureka Server dashboard.
 *
 * <p>Secures the Eureka dashboard with HTTP Basic Authentication.
 * Other microservices use the credentials from {@code application.yml}
 * to register with Eureka.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class EurekaSecurityConfig {

    /**
     * Configures HTTP security — requires authentication for all endpoints
     * and enables HTTP Basic for Eureka clients to register.
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                // Disable CSRF for Eureka client registration endpoints
                .ignoringRequestMatchers("/eureka/**")
            )
            .authorizeHttpRequests(auth -> auth
                // Allow actuator health endpoint without authentication
                .requestMatchers("/actuator/health").permitAll().requestMatchers("/api/auth/**").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {});

        return http.build();
    }
}

