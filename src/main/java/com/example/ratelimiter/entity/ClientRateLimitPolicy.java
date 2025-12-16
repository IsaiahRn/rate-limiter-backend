// src/main/java/com/example/ratelimiter/entity/ClientPolicyEntity.java
package com.example.ratelimiter.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "client_policies",
        indexes = {
                @Index(name = "idx_client_policies_client_id", columnList = "clientId", unique = true),
                @Index(name = "idx_client_policies_active", columnList = "active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRateLimitPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link policy to an existing user's username (client)
    @Column(nullable = false, unique = true, length = 120)
    private String clientId;

    @Column(nullable = false)
    private int windowSeconds;

    @Column(nullable = false)
    private int windowMaxRequests;

    @Column(nullable = false)
    private int monthlyMaxRequests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ThrottleMode throttleMode;

    @Column(nullable = false)
    private boolean active = true;
}
