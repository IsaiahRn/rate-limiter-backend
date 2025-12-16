package com.example.ratelimiter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckAllowanceRequest {

    @NotBlank
    private String clientId;

    // Optional, in case you later want per-route limits
    private String route;
}
