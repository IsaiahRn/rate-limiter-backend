package com.example.ratelimiter.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limiter.global")
public class RateLimiterProperties {
    private int windowSeconds;
    private long maxRequests;
    private long monthlyMaxRequests;
}
