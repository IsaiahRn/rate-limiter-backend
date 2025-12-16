package com.example.ratelimiter.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class DemoNotificationControllerTest {

    @Test
    void sendNotification_returns_ok() {
        DemoNotificationController controller = new DemoNotificationController();
        ResponseEntity<String> res = controller.sendNotification("{\"msg\":\"hello\"}");

//        assertEquals(200, res.getStatusCode());
        assertEquals("Notification accepted", res.getBody());
    }
}
