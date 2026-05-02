package com.fintrack.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * FinTrack Config Service — Eureka Discovery Server.
 *
 * <p>This service acts as the central service registry. All microservices
 * register themselves here on startup and discover other services through it.
 *
 * <p><b>Startup order:</b> This service MUST start first before any other
 * FinTrack microservice.
 *
 * <p>Default port: {@code 8761}
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

