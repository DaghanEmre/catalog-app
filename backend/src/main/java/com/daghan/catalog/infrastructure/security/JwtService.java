package com.daghan.catalog.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT Service for secure token generation and validation
 * ✅ Improved signature key management
 * ✅ Issuer validation for cross-service replay protection
 * ✅ Hardened validation rules
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final String issuer;
    private final long expirationMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {

        // Ensure secret is strong enough for HS256
        if (secret.length() < 32) {
            log.error("JWT_SECRET is too weak! HS256 requires at least 256 bits (32 bytes).");
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long.");
        }

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;

        log.info("JwtService initialized with issuer: {} and expiration: {}m", issuer, expirationMinutes);
    }

    public String generate(String username, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationMinutes * 60);

        log.debug("Generating JWT for subject: {}", username);

        return Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer) // ✅ Added issuer validation
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            throw e;
        }
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
