# ── Build Stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Copy pom files first — lets Docker cache the dependency layer
COPY pom.xml ./
COPY core/pom.xml core/
COPY auth-service/pom.xml auth-service/
COPY config-service/pom.xml config-service/
COPY gateway-service/pom.xml gateway-service/
COPY planning-service/pom.xml planning-service/
COPY service-template/pom.xml service-template/
RUN ./mvnw dependency:go-offline -B -q

# Copy source and build
COPY core/src core/src
COPY gateway-service/src gateway-service/src
RUN ./mvnw package -pl gateway-service -am -B -DskipTests -q

# ── Runtime Stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/gateway-service/target/gateway-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
