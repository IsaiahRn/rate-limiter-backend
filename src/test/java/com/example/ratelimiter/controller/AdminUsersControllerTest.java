package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.UserSummaryDto;
import com.example.ratelimiter.entity.AppUser;
import com.example.ratelimiter.entity.Role;
import com.example.ratelimiter.repository.AppUserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUsersControllerTest {

    private AutoCloseable mocks;

    @Mock
    private AppUserRepository userRepo;

    private AdminUsersController controller;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        controller = new AdminUsersController(userRepo);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void listClientUsers_maps_to_UserSummaryDto() {
        AppUser u1 = mock(AppUser.class);
        when(u1.getUsername()).thenReturn("client1");

        AppUser u2 = mock(AppUser.class);
        when(u2.getUsername()).thenReturn("client2");

        when(userRepo.findAllByRoleAndEnabledTrue(Role.CLIENT)).thenReturn(List.of(u1, u2));

        List<UserSummaryDto> result = controller.listClientUsers();

        assertEquals(2, result.size());
        assertEquals("client1", result.get(0).username());
        assertEquals("client2", result.get(1).username());

        verify(userRepo).findAllByRoleAndEnabledTrue(Role.CLIENT);
    }
}
