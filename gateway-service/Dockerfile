FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY gateway-service/target/gateway-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

