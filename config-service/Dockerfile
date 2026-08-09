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
COPY planning-service/pom.xml planning-service/
COPY service-template/pom.xml service-template/
RUN ./mvnw dependency:go-offline -B -q

# Copy source and build
COPY core/src core/src
COPY config-service/src config-service/src
RUN ./mvnw package -pl config-service -am -B -DskipTests -q

# ── Runtime Stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/config-service/target/config-service-*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
