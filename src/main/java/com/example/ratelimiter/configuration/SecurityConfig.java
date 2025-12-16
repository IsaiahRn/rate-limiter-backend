package com.example.ratelimiter.configuration;

import com.example.ratelimiter.filter.JwtAuthenticationFilter;
import com.example.ratelimiter.filter.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final AppProperties appProperties; // uses app.cors.allowed-origins

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // login + swagger
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // admin helpers
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // policies ADMIN-only
                        .requestMatchers(HttpMethod.GET, "/api/v1/rate-limits/clients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/rate-limits/clients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/rate-limits/clients/**").hasRole("ADMIN")

                        // demo: both roles
                        .requestMatchers("/api/v1/demo/**").hasAnyRole("ADMIN", "CLIENT")

                        .anyRequest().authenticated()
                )
                // JWT must run before anything that relies on Authentication
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Rate limiting needs authenticated username -> run AFTER JWT
                .addFilterAfter(rateLimitingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Prefer app.cors.allowed-origins (list). If blank, fall back to localhost.
        List<String> allowed = appProperties.getCors().getAllowedOrigins();
        if (allowed == null || allowed.isEmpty()) {
            allowed = List.of("http://localhost:4200");
        }

        // Handle comma-separated single value if someone sets it as a string-like list in YAML/env
        allowed = allowed.stream()
                .flatMap(v -> Arrays.stream(v.split(",")))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        config.setAllowedOrigins(allowed);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // expose retry-after & rate headers so Angular can read them if needed
        config.setExposedHeaders(List.of(
                "Authorization",
                "Retry-After",
                "X-RateLimit-Remaining-Window",
                "X-RateLimit-Remaining-Month",
                "X-RateLimit-Soft-Throttled"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
