package com.fintrack.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Stateless JWT utility service.
 *
 * <p>Uses JJWT 0.12.x fluent API with HMAC-SHA256 signing.
 * The secret key is loaded from {@code application.yml} and must be
 * <strong>at least 256 bits (32 bytes)</strong> for HS256.
 *
 * <p>This service handles only JWT operations — it does NOT touch the database.
 * Store-level operations (refresh token persistence) are in {@code AuthServiceImpl}.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class JwtService {

    @Value("${fintrack.jwt.secret}")
    private String secret;

    @Value("${fintrack.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    // ─────────────────────────────────────────────────────────────────────────
    // Token generation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates an access token with the subject set to the username.
     *
     * @param userDetails the authenticated user
     * @return a signed JWT string
     */
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, accessTokenExpiryMs);
    }

    /**
     * Generates a token with additional custom claims.
     *
     * @param extraClaims additional key-value pairs to embed in the token body
     * @param userDetails the authenticated user
     * @param expiryMs    token lifetime in milliseconds
     * @return a signed JWT string
     */
    public String generateToken(Map<String, Object> extraClaims,
                                UserDetails userDetails,
                                long expiryMs) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(getSigningKey())
                .compact();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Token validation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validates a JWT against the given user — checks signature, expiry, and subject match.
     *
     * @param token       the JWT to validate
     * @param userDetails the user to validate against
     * @return {@code true} if the token is valid for this user
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * @param token the JWT to check
     * @return {@code true} if the token has passed its expiry date
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Claims extraction
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extracts the {@code sub} (subject / username) claim from the token.
     *
     * @param token the JWT string
     * @return the username embedded in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiry date from the token.
     *
     * @param token the JWT string
     * @return the {@link Date} when the token expires
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extractor using a resolver function.
     *
     * @param token          the JWT string
     * @param claimsResolver a function mapping {@link Claims} to the desired value
     * @param <T>            the type of the extracted value
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Access token expiry helper
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @return access token lifetime in seconds (for the {@code expiresIn} response field)
     */
    public long getAccessTokenExpirySeconds() {
        return accessTokenExpiryMs / 1000;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

