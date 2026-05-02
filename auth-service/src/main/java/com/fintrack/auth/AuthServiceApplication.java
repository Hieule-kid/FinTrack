package com.fintrack.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FinTrack Auth Service.
 *
 * <p>Responsible for:
 * <ul>
 *   <li>User registration and login</li>
 *   <li>JWT access token + refresh token issuance</li>
 *   <li>Token validation and refresh</li>
 *   <li>Role-based permission enforcement</li>
 * </ul>
 *
 * <p>Default port: {@code 8081}
 *
 * <p>{@code scanBasePackages = "com.fintrack"} ensures the shared
 * {@code GlobalExceptionHandler} from the core module is picked up.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.fintrack")
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableScheduling
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

