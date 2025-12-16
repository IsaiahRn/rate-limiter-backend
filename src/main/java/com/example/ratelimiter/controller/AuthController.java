package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.AuthRequest;
import com.example.ratelimiter.dto.AuthResponse;
import com.example.ratelimiter.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Operation(summary = "Login and obtain JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }

        UserDetails user = (UserDetails) authentication.getPrincipal();
        String role = user.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)   // e.g. ROLE_ADMIN
                .orElse("ROLE_CLIENT");

        String roleShort = role.replace("ROLE_", "");  // ADMIN / CLIENT

        String token = jwtService.generateToken(user, roleShort);

        AuthResponse response = AuthResponse.builder()
                .username(user.getUsername())
                .role(roleShort)
                .token(token)
                .build();

        return ResponseEntity.ok(response);
    }
}
