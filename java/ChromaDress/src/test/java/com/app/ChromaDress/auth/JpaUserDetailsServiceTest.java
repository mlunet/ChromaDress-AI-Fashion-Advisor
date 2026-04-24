package com.app.ChromaDress.auth;

import com.app.ChromaDress.user.User;
import com.app.ChromaDress.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private JpaUserDetailsService service;

    @Test
    @DisplayName("Should return UserDetails successfully.")
    void testFindUserByUsername() {
        User mockUser = new User();
        mockUser.setUsername("username");
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = service.loadUserByUsername("username");

        assertNotNull(userDetails);
        assertEquals("username", userDetails.getUsername());
    }

    @Test
    @DisplayName("Should raise UsernameNotFoundException if user not found.")
    void testFindUserByUsernameNotFoundException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("unknown"));
    }
}
