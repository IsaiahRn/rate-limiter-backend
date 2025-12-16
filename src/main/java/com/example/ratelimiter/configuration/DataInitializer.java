package com.example.ratelimiter.configuration;

import com.example.ratelimiter.entity.AppUser;
import com.example.ratelimiter.entity.Role;
import com.example.ratelimiter.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.findByUsername("admin").isEmpty()) {
            AppUser admin = AppUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }

        if (userRepository.findByUsername("client").isEmpty()) {
            AppUser client = AppUser.builder()
                    .username("client")
                    .password(passwordEncoder.encode("client123"))
                    .role(Role.CLIENT)
                    .enabled(true)
                    .build();
            userRepository.save(client);
        }
    }
}
