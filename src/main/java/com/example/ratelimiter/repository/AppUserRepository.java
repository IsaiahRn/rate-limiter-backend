package com.example.ratelimiter.repository;

import com.example.ratelimiter.entity.AppUser;
import com.example.ratelimiter.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsernameAndRoleAndEnabledTrue(String username, Role role);

    List<AppUser> findAllByRoleAndEnabledTrue(Role role);
}
