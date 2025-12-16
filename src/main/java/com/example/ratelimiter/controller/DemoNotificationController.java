// src/main/java/com/example/ratelimiter/controller/DemoNotificationController.java
package com.example.ratelimiter.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoNotificationController {

    @Operation(summary = "Demo notification endpoint protected by rate limiter")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @PostMapping("/notify")
    public ResponseEntity<String> sendNotification(@RequestBody(required = false) String payload) {
        return ResponseEntity.ok("Notification accepted");
    }
}
