package com.example.ratelimiter.service;

import com.example.ratelimiter.entity.AppUser;
import com.example.ratelimiter.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())                 // already encoded
                .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                .accountLocked(false)
                .accountExpired(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}
