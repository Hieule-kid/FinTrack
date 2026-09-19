# Business Logic & System Flow

This repo hosts the **Eureka service-discovery server** for the FinTrack
microservices system (config-service). It doesn't contain business features
itself — its job is to let the other FinTrack services (Auth, Core, Planning,
...) find and talk to each other. This document captures the *why* behind
its configuration, extracted from source comments so the code itself can
stay comment-light.

## Service registry role and startup ordering

- This service acts as the central service registry for FinTrack. All other
  microservices register themselves here on startup and discover each other
  through it.
- **Startup order matters**: this service must start before any other
  FinTrack microservice, since those services need it available to register
  against and to resolve each other's addresses.
- It runs on port `8761` by default.
- Source: `src/main/java/com/fintrack/config/EurekaServerApplication.java`

## Eureka dashboard security and client registration

- The Eureka dashboard is secured with HTTP Basic Authentication so it isn't
  publicly browsable. Other FinTrack microservices authenticate with the
  same credentials (read from `application.yml`, backed by the
  `EUREKA_USERNAME`/`EUREKA_PASSWORD` environment variables) in order to
  register themselves with this server.
- CSRF protection is disabled specifically for `/eureka/**`, because Eureka
  clients register and send heartbeats via POST/PUT calls that can't carry a
  CSRF token — enforcing CSRF there would block all service registration.
- Source: `src/main/java/com/fintrack/config/EurekaSecurityConfig.java`

## Production credential handling

- The default Eureka dashboard credentials (`eureka` / `eureka123`) are
  placeholders for local development only. In production they must be
  overridden via the `SPRING_SECURITY_USER_NAME` and
  `SPRING_SECURITY_USER_PASSWORD` environment variables (surfaced here as
  `EUREKA_USERNAME`/`EUREKA_PASSWORD`) rather than left at their defaults.
- Source: `src/main/resources/application.yml`

## Why this server doesn't register with or query itself

- `eureka.client.register-with-eureka` is `false` because the discovery
  server itself is not a service other clients need to look up — it would
  be pointless (and circular) for it to register itself in its own registry.
- `eureka.client.fetch-registry` is `false` for the same reason: this server
  is the registry, so it doesn't need to fetch a copy of it to discover
  other services.
- Source: `src/main/resources/application.yml`

## Fast failover tuned for free-tier hosting

- `wait-time-in-ms-when-sync-empty` is set to `0` so newly started clients
  can register immediately instead of waiting for the server to reach a
  full peer sync (irrelevant here since this is a single-node registry).
- `enable-self-preservation` is disabled so that stale/dead service
  instances are evicted promptly after a cold start or redeploy rather than
  being kept around under Eureka's default self-preservation heuristics.
  This trade-off favors fast failover over registry stability, which fits
  the free-tier hosting environment where instances spin down and restart
  frequently.
- `eviction-interval-timer-in-ms` is lowered to `5000` (5s, vs. the Eureka
  default of 60s) so dead instances are cleaned out of the registry quickly,
  keeping service discovery accurate for the other FinTrack services.
- Source: `src/main/resources/application.yml`

## Multi-repo local stack composition

- FinTrack is split across multiple repos: this repo (config-service /
  Eureka), plus `Auth_Service_FinTrack` and `Planning_Service_FinTrack`,
  each with its own `docker-compose.yml`.
- The shared PostgreSQL container lives in this repo's compose file (not in
  auth-service's or planning-service's) purely because config-service
  doesn't need its own database, making this the natural place to host a
  shared DB for full local-stack development. The `postgres` default
  database is created first, and `postgres/init.sql` then creates the
  per-service application databases on top of it.
- To run the full local stack: start this repo's compose file first (it
  brings up the DB, pgAdmin, and config-service), then start each other
  service's own compose file with `EUREKA_HOST` pointing at this machine so
  they can register with this Eureka server.
- Source: `docker-compose.yml`
