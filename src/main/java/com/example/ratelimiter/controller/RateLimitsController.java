package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.ClientPolicyDto;
import com.example.ratelimiter.entity.Role;
import com.example.ratelimiter.exception.ApiError;
import com.example.ratelimiter.repository.AppUserRepository;
import com.example.ratelimiter.service.ClientPolicyService;
import com.example.ratelimiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rate-limits")
@RequiredArgsConstructor
public class RateLimitsController {

    private final ClientPolicyService policyService;
    private final AppUserRepository userRepo;
    private final RateLimiterService rateLimiterService;

    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ClientPolicyDto> listPolicies() {
        return policyService.listActive().stream()
                .map(p -> new ClientPolicyDto(
                        p.getClientId(),
                        p.getWindowSeconds(),
                        p.getWindowMaxRequests(),
                        p.getMonthlyMaxRequests(),
                        p.getThrottleMode()
                ))
                .toList();
    }

    @PostMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> upsertPolicy(@Valid @RequestBody ClientPolicyDto dto, HttpServletRequest req) {
        // Enforce the new requirement: policy must target an existing CLIENT user
        if (!userRepo.existsByUsernameAndRoleAndEnabledTrue(dto.clientId(), Role.CLIENT)) {
            Map<String, String> ve = new LinkedHashMap<>();
            ve.put("clientId", "Client user does not exist (must be an enabled CLIENT username)");
            ApiError body = ApiError.builder()
                    .error("Bad Request")
                    .message("Validation failed")
                    .path(req.getRequestURI())
                    .status(400)
                    .timestamp(Instant.now())
                    .validationErrors(ve)
                    .build();
            return ResponseEntity.badRequest().body(body);
        }

        var saved = policyService.upsert(dto);

        // reset counters so new policy takes effect immediately
        rateLimiterService.evict(saved.getClientId());

        return ResponseEntity.ok(new ClientPolicyDto(
                saved.getClientId(),
                saved.getWindowSeconds(),
                saved.getWindowMaxRequests(),
                saved.getMonthlyMaxRequests(),
                saved.getThrottleMode()
        ));
    }

    @DeleteMapping("/clients/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePolicy(@PathVariable String clientId) {
        policyService.deactivate(clientId);
        rateLimiterService.evict(clientId);
        return ResponseEntity.noContent().build();
    }
}
