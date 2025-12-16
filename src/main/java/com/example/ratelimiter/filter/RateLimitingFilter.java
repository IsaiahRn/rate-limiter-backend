package com.example.ratelimiter.filter;

import com.example.ratelimiter.service.RateLimitDecision;
import com.example.ratelimiter.service.RateLimiterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Apply ONLY to demo notify endpoint (client tests this)
        return !(path.equals("/api/v1/demo/notify") && "POST".equalsIgnoreCase(request.getMethod()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Key point: policy is linked to the authenticated username
        String clientId = auth.getName();

        RateLimitDecision decision = rateLimiterService.evaluateAndConsumeForUser(clientId);

        // Expose rate information
        response.setHeader("X-RateLimit-Remaining-Window", String.valueOf(decision.getRemainingInWindow()));
        response.setHeader("X-RateLimit-Remaining-Month", String.valueOf(decision.getRemainingInMonth()));
        if (decision.isSoftThrottled()) {
            response.setHeader("X-RateLimit-Soft-Throttled", "true");
        }

        if (!decision.isAllowed()) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(Math.max(0, decision.getRetryAfterSeconds())));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(decision));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
