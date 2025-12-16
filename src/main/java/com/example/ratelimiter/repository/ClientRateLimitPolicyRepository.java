
package com.example.ratelimiter.repository;

import com.example.ratelimiter.entity.ClientRateLimitPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRateLimitPolicyRepository extends JpaRepository<ClientRateLimitPolicy, Long> {
    List<ClientRateLimitPolicy> findAllByActiveTrueOrderByClientIdAsc();

    Optional<ClientRateLimitPolicy> findByClientIdAndActiveTrue(String clientId);

    Optional<ClientRateLimitPolicy> findByClientId(String clientId);
}
