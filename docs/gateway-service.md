# FinTrack :: Gateway Service

> Single entry point for all external client requests to the FinTrack platform.
> Routes incoming HTTP requests to the appropriate downstream microservice via Spring Cloud Gateway.

---

## ℹ️ Overview

| Property | Value |
|---|---|
| **External Port** | `8088` |
| **Internal Port** | `8080` |
| **Technology** | Spring Cloud Gateway |
| **Database** | None |
| **Depends on** | `config-service` (Eureka), `auth-service` |

---

## 🚀 How to Start

```bash
# Prerequisite: config-service must be running (auth-service recommended)
cd gateway-service
../mvnw spring-boot:run
```

**Docker:**
```bash
docker compose up gateway-service
```

---

## 🗺️ Route Table

All external clients should target `http://localhost:8088`.

| External Path | Routed to | Notes |
|---|---|---|
| `/api/v1/auth/**` | `auth-service` (port 8081) | Registration, login, token refresh, profile, logout |
| `/api/v1/users/**` | `auth-service` (port 8081) | User profile operations |
| `/api/v1/plans/**` | `planning-service` (port 8090) | Savings goals and milestone tracking |

Routes are resolved via **Eureka load-balancing** (`lb://service-name`).

---

## 📁 Source Structure

```
gateway-service/src/main/java/com/fintrack/gateway/
└── GatewayServiceApplication.java   # Entry point — @SpringBootApplication
```

Route configuration lives in `src/main/resources/application.yml`.

---

## ⚙️ Configuration (`application.yml`)

```yaml
server:
  port: 8080          # internal port (Docker maps 8088 → 8080)

spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/auth/**,/api/v1/users/**

        - id: planning-service
          uri: lb://planning-service
          predicates:
            - Path=/api/v1/plans/**

eureka:
  client:
    service-url:
      defaultZone: http://${EUREKA_USERNAME}:${EUREKA_PASSWORD}@${EUREKA_HOST}:8761/eureka/
```

---

## 🐳 Docker Compose

```yaml
gateway-service:
  build:
    context: .
    dockerfile: gateway-service/Dockerfile
  image: fintrack/gateway-service:latest
  restart: always
  environment:
    EUREKA_HOST: config-service
    EUREKA_USERNAME: eureka
    EUREKA_PASSWORD: eureka123
  ports:
    - "8088:8080"      # external:internal
  depends_on:
    config-service:
      condition: service_healthy
    auth-service:
      condition: service_started
```

---

## ⚙️ Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `EUREKA_HOST` | `localhost` | Hostname of `config-service` |
| `EUREKA_USERNAME` | `eureka` | Eureka HTTP Basic username |
| `EUREKA_PASSWORD` | `eureka123` | Eureka HTTP Basic password |

---

## 🧪 Quick Test via Gateway

```bash
# All requests go through port 8088

# Register
curl -s -X POST http://localhost:8088/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Alice","username":"alice","email":"alice@example.com","password":"pass123"}'

# Login
curl -s -X POST http://localhost:8088/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"emailOrUsername":"alice","password":"pass123"}'

# List plans (replace <token>)
curl -s http://localhost:8088/api/v1/plans \
  -H "Authorization: Bearer <token>"
```

---

## 📌 Notes

- The gateway does **not** perform JWT validation — authentication is enforced by each downstream service
- pgAdmin runs on port `8080`; the gateway is mapped to `8088` externally to avoid conflict
- Service discovery is dynamic: add a new service to Eureka and create a new route entry in `application.yml` — no gateway code changes required

