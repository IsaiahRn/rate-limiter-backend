package com.example.ratelimiter.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Allow Angular dev origin
                .allowedOrigins("http://localhost:4200")
                // Methods your API uses
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // Headers you accept (include X-Client-Id for demo endpoint)
                .allowedHeaders("*")
                // Headers the frontend can read (for rate limit info)
                .exposedHeaders(
                        "Retry-After",
                        "X-RateLimit-Limit",
                        "X-RateLimit-Remaining",
                        "X-RateLimit-Remaining-Window",
                        "X-RateLimit-Remaining-Month",
                        "X-RateLimit-Soft-Throttled"
                )
                // No cookies for this project
                .allowCredentials(false)
                // Cache preflight results for 1 hour
                .maxAge(3600);
    }
}
