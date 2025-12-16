package com.example.ratelimiter.dto;

import com.example.ratelimiter.entity.ThrottleMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientRateLimitResponse {
    private String clientId;
    private int windowSeconds;
    private long windowMaxRequests;
    private long monthlyMaxRequests;
    private ThrottleMode throttleMode;
}
