package com.example.ratelimiter.configuration;

import com.example.ratelimiter.entity.AppUser;
import com.example.ratelimiter.entity.Role;
import com.example.ratelimiter.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final AppUserRepository userRepo;
    private final PasswordEncoder encoder;

    /**
     * Controls whether seeding runs at startup.
     * Env: APP_SEED_ENABLED=true|false
     */
    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    /**
     * If true, resets the seeded users' passwords on startup.
     * Use this ONCE on cloud to recover access, then set it back to false.
     * Env: APP_SEED_RESET_PASSWORDS=true|false
     */
    @Value("${app.seed.reset-passwords:false}")
    private boolean resetPasswords;

    @Bean
    CommandLineRunner seedUsers() {
        return args -> {
            if (!seedEnabled) return;

            upsertSeedUser("admin", "admin@123", Role.ADMIN);
            upsertSeedUser("client1", "client@123", Role.CLIENT);
            upsertSeedUser("client2", "client@123", Role.CLIENT);
            upsertSeedUser("client3", "client@123", Role.CLIENT);
            upsertSeedUser("client4", "client@123", Role.CLIENT);
            upsertSeedUser("client5", "client@123", Role.CLIENT);
        };
    }

    private void upsertSeedUser(String username, String rawPassword, Role role) {
        AppUser user = userRepo.findByUsername(username).orElse(null);

        if (user == null) {
            userRepo.save(AppUser.builder()
                    .username(username)
                    .password(encoder.encode(rawPassword))
                    .role(role)
                    .enabled(true)
                    .build());
            return;
        }

        boolean changed = false;

        if (user.getRole() != role) {
            user.setRole(role);
            changed = true;
        }
        if (!user.isEnabled()) {
            user.setEnabled(true);
            changed = true;
        }

        if (resetPasswords) {
            user.setPassword(encoder.encode(rawPassword));
            changed = true;
        }

        if (changed) {
            userRepo.save(user);
        }
    }
}
