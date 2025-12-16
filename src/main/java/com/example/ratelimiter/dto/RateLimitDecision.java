package com.example.ratelimiter.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RateLimitDecision {
    private boolean allowed;
    private boolean softThrottled;
    private long retryAfterSeconds;
    private String reason;
    private long remainingInWindow;
    private long remainingInMonth;
}
