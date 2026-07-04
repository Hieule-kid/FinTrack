# FinTrack :: Config Service (Eureka Discovery Server)

> Centralized service registry for the FinTrack platform.
> Every other microservice registers here on startup and discovers its peers through it.

---

## ℹ️ Overview

| Property | Value |
|---|---|
| **Port** | `8761` |
| **Technology** | Spring Cloud Netflix Eureka Server |
| **Security** | HTTP Basic Authentication |
| **Database** | None |
| **Must start** | **First** — all other services depend on it |

---

## 🚀 How to Start

```bash
cd config-service
../mvnw spring-boot:run
# ✅ Ready when you see: "Started EurekaServerApplication"
```

**Docker:**
```bash
docker compose up config-service
```

---

## 🌐 Endpoints

| URL | Description |
|---|---|
| `http://localhost:8761` | Eureka dashboard — lists all registered services |
| `http://localhost:8761/eureka/apps` | Raw XML list of registered service instances |
| `http://localhost:8761/actuator/health` | Health check (no auth required) |

### Default Credentials

| Field | Value |
|---|---|
| Username | `eureka` |
| Password | `eureka123` |

> ⚠️ Change these in production via `EUREKA_USERNAME` / `EUREKA_PASSWORD` environment variables.

---

## ⚙️ Configuration (`application.yml`)

```yaml
server:
  port: 8761

spring:
  application:
    name: config-service
  security:
    user:
      name: ${EUREKA_USERNAME:eureka}
      password: ${EUREKA_PASSWORD:eureka123}

eureka:
  instance:
    hostname: ${EUREKA_HOST:localhost}
  client:
    register-with-eureka: false  # This IS the server — don't self-register
    fetch-registry: false
  server:
    wait-time-in-ms-when-sync-empty: 0
```

---

## 🔐 Security Model

Security is configured in `EurekaSecurityConfig.java`:

- All endpoints require **HTTP Basic** authentication (`username: eureka`, `password: eureka123`)
- The `/eureka/**` registration path has **CSRF disabled** so clients can register
- `/actuator/health` is **public** (used by Docker Compose health checks)

### How Other Services Register

Each microservice's `application.yml` includes:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://${EUREKA_USERNAME}:${EUREKA_PASSWORD}@${EUREKA_HOST}:8761/eureka/
```

---

## 📁 Source Structure

```
config-service/src/main/java/com/fintrack/config/
├── EurekaServerApplication.java   # Main class — @EnableEurekaServer
└── EurekaSecurityConfig.java      # HTTP Basic auth + CSRF config
```

---

## 🐳 Docker Compose

```yaml
config-service:
  build:
    context: .
    dockerfile: config-service/Dockerfile
  image: fintrack/config-service:latest
  restart: always
  environment:
    EUREKA_USERNAME: eureka
    EUREKA_PASSWORD: eureka123
    EUREKA_HOST: config-service
  ports:
    - "8761:8761"
  healthcheck:
    test: ["CMD-SHELL", "curl -sf http://eureka:eureka123@localhost:8761/actuator/health | grep UP || exit 1"]
    interval: 15s
    timeout: 5s
    retries: 8
```

All other services use `depends_on: config-service: condition: service_healthy` to wait for this service before starting.

