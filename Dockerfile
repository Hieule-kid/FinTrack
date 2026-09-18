# ── Build Stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Copy pom first — lets Docker cache the dependency layer
COPY pom.xml ./
RUN ./mvnw dependency:go-offline -B -q

# Copy source and build
COPY src src
RUN ./mvnw package -B -DskipTests -q

# ── Runtime Stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/config-service-*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
