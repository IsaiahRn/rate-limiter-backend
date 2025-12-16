package com.example.ratelimiter.service;

import com.example.ratelimiter.configuration.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Robust JWT utility:
 * - Reads secret from app.jwt.secret (env: APP_JWT_SECRET)
 * - Ensures key length is safe for HS256 (>= 32 bytes)
 * - Includes role claim (role: ADMIN/CLIENT) for convenience
 */
@Service
public class JwtService {

    // 1 hour token validity
    private static final long JWT_EXPIRATION_MILLIS = 60 * 60 * 1000L;

    private static final String FALLBACK_DEV_SECRET =
            "dev-secret-key-please-change-me-dev-secret-key-please-change-me";

    private final SecretKey signingKey;

    public JwtService(AppProperties props) {
        String secret = null;
        if (props != null && props.getJwt() != null) {
            secret = props.getJwt().getSecret();
        }

        if (secret == null || secret.isBlank() || secret.length() < 32) {
            secret = FALLBACK_DEV_SECRET;
        }

        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails, String roleShort) {
        // roleShort is "ADMIN" or "CLIENT"
        Map<String, Object> extraClaims = Map.of("role", roleShort);
        return buildToken(extraClaims, userDetails.getUsername());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
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
