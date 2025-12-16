package com.example.ratelimiter.exception;

public class ClientPolicyNotFoundException extends RuntimeException {

    public ClientPolicyNotFoundException(String clientId) {
        super("No active rate limit policy configured for client " + clientId);
    }
}
