package com.example.ratelimiter.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rateLimiterOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rate Limiter API")
                        .description("API for managing and enforcing per-client and global rate limits")
                        .version("v1"));
    }
}
