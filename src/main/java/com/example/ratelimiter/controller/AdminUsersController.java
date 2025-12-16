package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.UserSummaryDto;
import com.example.ratelimiter.entity.Role;
import com.example.ratelimiter.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsersController {

    private final AppUserRepository userRepo;

    @GetMapping("/clients")
    public List<UserSummaryDto> listClientUsers() {
        return userRepo.findAllByRoleAndEnabledTrue(Role.CLIENT)
                .stream()
                .map(u -> new UserSummaryDto(u.getUsername()))
                .toList();
    }
}
