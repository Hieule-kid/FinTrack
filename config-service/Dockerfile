FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY config-service/target/config-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

