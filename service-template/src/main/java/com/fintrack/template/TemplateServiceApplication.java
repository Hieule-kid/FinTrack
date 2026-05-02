package com.fintrack.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * FinTrack Service Template Application.
 *
 * <p>This is a copy-paste starting point for a new microservice.
 * Steps to create a new service from this template:
 * <ol>
 *   <li>Copy the {@code service-template} module to a new directory</li>
 *   <li>Rename all {@code template} references to your domain name</li>
 *   <li>Update {@code application.yml} (service name, port, DB name)</li>
 *   <li>Register the new module in the root {@code pom.xml}</li>
 * </ol>
 *
 * <p>Default port: {@code 8082}
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.fintrack")
@EnableDiscoveryClient
@EnableJpaAuditing
public class TemplateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateServiceApplication.class, args);
    }
}
