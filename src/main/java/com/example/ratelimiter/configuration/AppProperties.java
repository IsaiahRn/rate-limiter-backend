package com.example.ratelimiter.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();

    @Data
    public static class Jwt {
        /**
         * Secret used to sign JWTs (set via env var APP_JWT_SECRET in prod)
         */
        private String secret;
    }

    @Data
    public static class Cors {
        /**
         * Allowed origins for browser apps (Angular dev + Netlify)
         */
        private List<String> allowedOrigins = List.of("http://localhost:4200");
    }
}
