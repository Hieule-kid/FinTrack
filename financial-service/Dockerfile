# Multi-stage build: compile and package
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy parent POM and necessary modules
COPY pom.xml .
COPY core ./core
COPY financial-service ./financial-service

# Build financial-service
RUN mvn clean package -pl financial-service -am -DskipTests -q

# Runtime stage: minimal base image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the executable JAR from the builder stage
COPY --from=builder /build/financial-service/target/financial-service-1.0.0-SNAPSHOT.jar app.jar

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8083

