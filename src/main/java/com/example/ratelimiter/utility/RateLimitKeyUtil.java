package com.example.ratelimiter.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class RateLimitKeyUtil {

    private static final DateTimeFormatter WINDOW_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmm"); // minute-level window

    private RateLimitKeyUtil() {}

    public static String clientWindowKey(String clientId, LocalDateTime now) {
        return "rl:client:" + clientId + ":win:" + now.format(WINDOW_FORMATTER);
    }

    public static String clientMonthlyKey(String clientId, LocalDateTime now) {
        String ym = now.getYear() + String.format("%02d", now.getMonthValue());
        return "rl:client:" + clientId + ":month:" + ym;
    }

    public static String globalWindowKey(LocalDateTime now) {
        return "rl:global:win:" + now.format(WINDOW_FORMATTER);
    }

    public static String globalMonthlyKey(LocalDateTime now) {
        String ym = now.getYear() + String.format("%02d", now.getMonthValue());
        return "rl:global:month:" + ym;
    }
}
