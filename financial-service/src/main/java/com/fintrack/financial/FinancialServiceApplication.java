package com.fintrack.financial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the FinTrack Financial Service.
 *
 * <p>Provides financial planning and savings goal tracking capabilities.
 * Registers with the Eureka Service Discovery for inter-service communication.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
public class FinancialServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialServiceApplication.class, args);
    }
}

