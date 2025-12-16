package com.example.ratelimiter.dto;

import com.example.ratelimiter.entity.ThrottleMode;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRateLimitRequest {

    @NotBlank
    private String clientId;

    @Min(1)
    private int windowSeconds;

    @Min(1)
    @JsonAlias("maxRequestsPerWindow")
    private int windowMaxRequests;

    @Min(1)
    @JsonAlias("monthlyQuota")
    private int monthlyMaxRequests;

    private ThrottleMode throttleMode;
}
