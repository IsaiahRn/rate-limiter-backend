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
            seedIfMissing("client1", "client@123", Role.CLIENT);
            seedIfMissing("client2", "client@123", Role.CLIENT);
            seedIfMissing("client3", "client@123", Role.CLIENT);
            seedIfMissing("client4", "client@123", Role.CLIENT);
            seedIfMissing("client5", "client@123", Role.CLIENT);
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
