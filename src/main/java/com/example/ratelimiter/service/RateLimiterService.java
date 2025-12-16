package com.example.ratelimiter.service;

import com.example.ratelimiter.entity.ClientRateLimitPolicy;
import com.example.ratelimiter.entity.ThrottleMode;
import com.example.ratelimiter.repository.ClientRateLimitPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final ClientRateLimitPolicyRepository policyRepo;

    private static final ZoneId ZONE = ZoneOffset.UTC;

    private static final class Counters {
        long windowStartEpochSec;
        int windowCount;

        YearMonth month;
        int monthCount;

        Counters(long nowEpochSec, YearMonth ym) {
            this.windowStartEpochSec = nowEpochSec;
            this.windowCount = 0;
            this.month = ym;
            this.monthCount = 0;
        }
    }

    private final ConcurrentHashMap<String, Counters> store = new ConcurrentHashMap<>();

    public RateLimitDecision evaluateAndConsumeForUser(String clientId) {
        ClientRateLimitPolicy policy = policyRepo.findByClientIdAndActiveTrue(clientId).orElse(null);

        // If no policy yet, allow (keeps existing behavior “working”, but now it’s linked by username).
        if (policy == null) {
            return RateLimitDecision.builder()
                    .allowed(true)
                    .softThrottled(false)
                    .reason("NO_POLICY")
                    .retryAfterSeconds(0)
                    .remainingInWindow(Integer.MAX_VALUE)
                    .remainingInMonth(Integer.MAX_VALUE)
                    .build();
        }

        final long now = Instant.now().getEpochSecond();
        final YearMonth ymNow = YearMonth.now(ZONE);

        Counters c = store.computeIfAbsent(clientId, k -> new Counters(now, ymNow));

        synchronized (c) {
            // Reset rolling window
            if (now - c.windowStartEpochSec >= policy.getWindowSeconds()) {
                c.windowStartEpochSec = now;
                c.windowCount = 0;
            }

            // Reset monthly bucket
            if (!ymNow.equals(c.month)) {
                c.month = ymNow;
                c.monthCount = 0;
            }

            boolean windowExceeded = c.windowCount >= policy.getWindowMaxRequests();
            boolean monthExceeded = c.monthCount >= policy.getMonthlyMaxRequests();

            long retryAfter = 0;
            if (windowExceeded) {
                long elapsed = now - c.windowStartEpochSec;
                retryAfter = Math.max(0, policy.getWindowSeconds() - elapsed);
            } else if (monthExceeded) {
                ZonedDateTime zdt = Instant.ofEpochSecond(now).atZone(ZONE);
                ZonedDateTime firstNextMonth = zdt.withDayOfMonth(1).plusMonths(1).toLocalDate().atStartOfDay(ZONE);
                retryAfter = Math.max(0, Duration.between(zdt, firstNextMonth).getSeconds());
            }

            // HARD => block, SOFT => allow but mark softThrottled
            if (windowExceeded || monthExceeded) {
                if (policy.getThrottleMode() == ThrottleMode.HARD) {
                    return RateLimitDecision.builder()
                            .allowed(false)
                            .softThrottled(false)
                            .reason(windowExceeded ? "WINDOW_QUOTA_EXCEEDED" : "MONTHLY_QUOTA_EXCEEDED")
                            .retryAfterSeconds(retryAfter)
                            .remainingInWindow(Math.max(0, policy.getWindowMaxRequests() - c.windowCount))
                            .remainingInMonth(Math.max(0, policy.getMonthlyMaxRequests() - c.monthCount))
                            .build();
                }

                // SOFT throttling: still consume
                c.windowCount++;
                c.monthCount++;

                return RateLimitDecision.builder()
                        .allowed(true)
                        .softThrottled(true)
                        .reason(windowExceeded ? "SOFT_THROTTLED_WINDOW" : "SOFT_THROTTLED_MONTH")
                        .retryAfterSeconds(retryAfter)
                        .remainingInWindow(Math.max(0, policy.getWindowMaxRequests() - c.windowCount))
                        .remainingInMonth(Math.max(0, policy.getMonthlyMaxRequests() - c.monthCount))
                        .build();
            }

            // Consume (allowed)
            c.windowCount++;
            c.monthCount++;

            return RateLimitDecision.builder()
                    .allowed(true)
                    .softThrottled(false)
                    .reason("OK")
                    .retryAfterSeconds(0)
                    .remainingInWindow(Math.max(0, policy.getWindowMaxRequests() - c.windowCount))
                    .remainingInMonth(Math.max(0, policy.getMonthlyMaxRequests() - c.monthCount))
                    .build();
        }
    }

    public void evict(String clientId) {
        store.remove(clientId);
    }
}
