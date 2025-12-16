package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.AuthRequest;
import com.example.ratelimiter.dto.AuthResponse;
import com.example.ratelimiter.service.JwtService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AutoCloseable mocks;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        controller = new AuthController(authenticationManager, jwtService);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void login_success_returns_token_and_role() {
        AuthRequest req = new AuthRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        User principal = new User(
                "admin",
                "pw",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        when(jwtService.generateToken(eq(principal), eq("ADMIN"))).thenReturn("jwt-token");

        ResponseEntity<AuthResponse> res = controller.login(req);

//        assertEquals(200, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("admin", res.getBody().getUsername());
        assertEquals("ADMIN", res.getBody().getRole());
        assertEquals("jwt-token", res.getBody().getToken());
    }

    @Test
    void login_bad_credentials_throws() {
        AuthRequest req = new AuthRequest();
        req.setUsername("admin");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class, () -> controller.login(req));
    }
}
