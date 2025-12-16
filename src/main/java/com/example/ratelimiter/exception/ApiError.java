package com.example.ratelimiter.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
    private String error;
    private String message;
    private String path;
    private int status;
    private Instant timestamp;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> validationErrors;
}
