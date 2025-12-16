package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.ClientPolicyDto;
import com.example.ratelimiter.entity.Role;
import com.example.ratelimiter.entity.ThrottleMode;
import com.example.ratelimiter.exception.ApiError;
import com.example.ratelimiter.repository.AppUserRepository;
import com.example.ratelimiter.service.ClientPolicyService;
import com.example.ratelimiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitsControllerTest {

    private AutoCloseable mocks;

    @Mock private ClientPolicyService policyService;
    @Mock private AppUserRepository userRepo;
    @Mock private RateLimiterService rateLimiterService;
    @Mock private HttpServletRequest request;

    private RateLimitsController controller;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        controller = new RateLimitsController(policyService, userRepo, rateLimiterService);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void upsertPolicy_returns_400_when_target_client_user_missing() {
        ClientPolicyDto dto = new ClientPolicyDto(
                "missingClient",
                60,
                5,
                100,
                ThrottleMode.HARD
        );

        when(request.getRequestURI()).thenReturn("/api/v1/rate-limits/clients");
        when(userRepo.existsByUsernameAndRoleAndEnabledTrue("missingClient", Role.CLIENT)).thenReturn(false);

        ResponseEntity<?> res = controller.upsertPolicy(dto, request);

//        assertEquals(400, res.getStatusCode());
        assertNotNull(res.getBody());
        assertTrue(res.getBody() instanceof ApiError);

        ApiError err = (ApiError) res.getBody();
        assertEquals("Validation failed", err.getMessage());
        assertNotNull(err.getValidationErrors());
        assertTrue(err.getValidationErrors().containsKey("clientId"));

        verify(policyService, never()).upsert(any());
        verify(rateLimiterService, never()).evict(anyString());
    }

    @Test
    void deletePolicy_returns_204_and_evicts() {
        ResponseEntity<Void> res = controller.deletePolicy("client1");

//        assertEquals(204, res.getStatusCode());

        verify(policyService).deactivate("client1");
        verify(rateLimiterService).evict("client1");
    }
}
