package com.example.ratelimiter.configuration;

import com.example.ratelimiter.entity.AppUser;
import com.example.ratelimiter.entity.Role;
import com.example.ratelimiter.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final AppUserRepository userRepo;
    private final PasswordEncoder encoder;

    @Bean
    CommandLineRunner seedUsers() {
        return args -> {
            seedIfMissing("admin", "admin@123", Role.ADMIN);
            seedIfMissing("client", "client@123", Role.CLIENT);

            // Optional extra demo clients (use any you want)
            seedIfMissing("client-soft", "client@123", Role.CLIENT);
            seedIfMissing("client-month", "client@123", Role.CLIENT);
            seedIfMissing("client-window", "client@123", Role.CLIENT);
        };
    }

    private void seedIfMissing(String username, String rawPassword, Role role) {
        userRepo.findByUsername(username).ifPresentOrElse(
                u -> { /* already exists */ },
                () -> userRepo.save(AppUser.builder()
                        .username(username)
                        .password(encoder.encode(rawPassword))
                        .role(role)
                        .enabled(true)
                        .build())
        );
    }
}
