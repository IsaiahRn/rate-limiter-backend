package com.example.ratelimiter.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckAllowanceResponse {

    private String clientId;
    private RateLimitDecision decision;
}
