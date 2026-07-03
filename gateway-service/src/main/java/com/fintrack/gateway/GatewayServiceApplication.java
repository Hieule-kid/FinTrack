package com.fintrack.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for FinTrack API Gateway.
 *
 * <p>The gateway receives external requests and forwards them to downstream
 * services using Spring Cloud Gateway routes.
 */
@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}

