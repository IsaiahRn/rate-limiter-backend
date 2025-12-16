package com.example.ratelimiter.dto;

import com.example.ratelimiter.entity.ThrottleMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClientPolicyDto(
        @NotBlank String clientId,
        @Min(1) int windowSeconds,
        @Min(1) int windowMaxRequests,
        @Min(1) int monthlyMaxRequests,
        @NotNull ThrottleMode throttleMode
) {}
