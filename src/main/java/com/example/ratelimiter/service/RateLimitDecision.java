package com.example.ratelimiter.service;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateLimitDecision {
    private boolean allowed;
    private boolean softThrottled;
    private String reason;
    private long retryAfterSeconds;
    private int remainingInWindow;
    private int remainingInMonth;
}
