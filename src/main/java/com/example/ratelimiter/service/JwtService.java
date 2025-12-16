package com.example.ratelimiter.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Minimal, robust JWT utility.
 * - Uses a default dev secret if security.jwt.secret is not defined
 * - Ensures the key is always long enough so bean creation never fails
 */
@Service
public class JwtService {

    // 1 hour token validity (tweak if you like)
    private static final long JWT_EXPIRATION_MILLIS = 60 * 60 * 1000L;

    private final SecretKey signingKey;

    public JwtService(
            @Value("${security.jwt.secret:dev-secret-key-please-change-me-dev-secret-key-please-change-me}") String secret
    ) {
        // Ensure at least 32 bytes for HS256
        if (secret == null || secret.length() < 32) {
            secret = "dev-secret-key-please-change-me-dev-secret-key-please-change-me";
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Generate a JWT for the given user and role.
     */
    public String generateToken(UserDetails userDetails, String role) {
        Map<String, Object> extraClaims = Map.of("role", role);
        return buildToken(extraClaims, userDetails.getUsername());
    }

    /**
     * Extract username (subject) from token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Check if token belongs to the given user and is not expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + JWT_EXPIRATION_MILLIS);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
