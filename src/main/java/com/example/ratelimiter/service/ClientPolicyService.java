package com.example.ratelimiter.service;

import com.example.ratelimiter.dto.ClientPolicyDto;
import com.example.ratelimiter.entity.ClientRateLimitPolicy;
import com.example.ratelimiter.repository.ClientRateLimitPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientPolicyService {

    private final ClientRateLimitPolicyRepository repo;

    public List<ClientRateLimitPolicy> listActive() {
        return repo.findAllByActiveTrueOrderByClientIdAsc();
    }

    public ClientRateLimitPolicy upsert(ClientPolicyDto dto) {
        ClientRateLimitPolicy entity = repo.findByClientId(dto.clientId())
                .orElseGet(ClientRateLimitPolicy::new);

        entity.setClientId(dto.clientId());
        entity.setWindowSeconds(dto.windowSeconds());
        entity.setWindowMaxRequests(dto.windowMaxRequests());
        entity.setMonthlyMaxRequests(dto.monthlyMaxRequests());
        entity.setThrottleMode(dto.throttleMode());
        entity.setActive(true);

        return repo.save(entity);
    }

    public void deactivate(String clientId) {
        ClientRateLimitPolicy entity = repo.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found for client: " + clientId));
        entity.setActive(false);
        repo.save(entity);
    }
}
