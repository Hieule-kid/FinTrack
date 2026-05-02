FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY auth-service/target/auth-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

