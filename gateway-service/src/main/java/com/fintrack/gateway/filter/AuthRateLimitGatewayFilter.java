package com.fintrack.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Limits public authentication requests before they reach auth-service. Counters
 * expire after the configured TTL, preventing stale client addresses from being
 * retained indefinitely. This local implementation is suitable for one gateway
 * instance; use a shared Redis rate limiter when the gateway is scaled out.
 */
@Component
public class AuthRateLimitGatewayFilter implements GlobalFilter, Ordered {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REGISTER_PATH = "/api/v1/auth/register";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";
    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong requestCount = new AtomicLong();

    @Value("${fintrack.gateway.rate-limit.ttl-seconds:60}")
    private long ttlSeconds;

    @Value("${fintrack.gateway.rate-limit.login-per-window:10}")
    private int loginLimit;

    @Value("${fintrack.gateway.rate-limit.register-per-window:5}")
    private int registerLimit;

    @Value("${fintrack.gateway.rate-limit.refresh-per-window:20}")
    private int refreshLimit;

    /**
     * Only enable this when an upstream proxy is controlled and strips incoming
     * X-Forwarded-For headers. Otherwise clients could spoof their rate-limit key.
     */
    @Value("${fintrack.gateway.rate-limit.trust-forwarded-for:false}")
    private boolean trustForwardedFor;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getRequest().getMethod() != HttpMethod.POST) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getPath().value();
        int limit = limitFor(path);
        if (limit == 0) {
            return chain.filter(exchange);
        }

        long now = System.currentTimeMillis();
        if (requestCount.incrementAndGet() % 100 == 0) {
            windows.entrySet().removeIf(entry -> entry.getValue().expiresAt <= now);
        }

        String key = path + ':' + clientKey(exchange);
        Window window = windows.get(key);
        if (window == null && windows.size() >= MAX_TRACKED_CLIENTS) {
            return tooManyRequests(exchange);
        }

        Window updated = windows.compute(key, (ignored, current) -> {
            if (current == null || current.expiresAt <= now) {
                return new Window(1, now + ttlMillis());
            }
            return new Window(current.requests + 1, current.expiresAt);
        });

        return updated.requests > limit ? tooManyRequests(exchange) : chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private int limitFor(String path) {
        if (LOGIN_PATH.equals(path)) {
            return loginLimit;
        }
        if (REGISTER_PATH.equals(path)) {
            return registerLimit;
        }
        return REFRESH_PATH.equals(path) ? refreshLimit : 0;
    }

    private String clientKey(ServerWebExchange exchange) {
        if (trustForwardedFor) {
            String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",", 2)[0].trim();
            }
        }
        InetSocketAddress address = exchange.getRequest().getRemoteAddress();
        return address == null || address.getAddress() == null
                ? "unknown"
                : address.getAddress().getHostAddress();
    }

    private long ttlMillis() {
        return Math.max(1, ttlSeconds) * 1_000;
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        byte[] body = "{\"code\":1006,\"message\":\"Too many authentication attempts. Please try again later.\",\"data\":null}"
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set("Retry-After", String.valueOf(Math.max(1, ttlSeconds)));
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    private record Window(int requests, long expiresAt) { }
}
