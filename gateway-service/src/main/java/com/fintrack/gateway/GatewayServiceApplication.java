package com.fintrack.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FinTrack API Gateway Service.
 *
 * <p>Single entry point for all external client requests to the FinTrack platform.
 * Routes incoming HTTP requests to the appropriate downstream microservice using
 * Spring Cloud Gateway with routes defined in {@code application.yml}.
 *
 * <p>Registered services routed through this gateway:
 * <ul>
 *   <li>{@code /api/v1/auth/**}  → {@code auth-service}      (port 8081)</li>
 *   <li>{@code /api/v1/plans/**} → {@code planning-service}  (port 8090)</li>
 * </ul>
 *
 * <p>Service discovery is provided by {@code config-service} (Eureka) — the gateway
 * resolves service names via Eureka load-balancing ({@code lb://service-name}).
 *
 * <p>Default port: {@code 8088} (externally; gateway listens on {@code 8080} internally)
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}

