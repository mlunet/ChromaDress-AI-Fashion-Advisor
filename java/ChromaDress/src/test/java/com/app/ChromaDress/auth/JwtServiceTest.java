package com.app.ChromaDress.auth;

import com.app.ChromaDress.user.User;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User mockUser;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretString", "QuestaEUnaChiaveSegretaMoltoLungaPerSuperareIControlliDiSicurezza2026");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", Duration.ofDays(7));

        mockUser = new User(1L, "fake_name", "fake_password", "fake-email", Collections.emptyList());
        mockUserDetails = new UserPrincipal(mockUser);
    }

    @Test
    @DisplayName("Should generate a token and extract the correct username.")
    void testGenerateToken() {
        String token = jwtService.generateToken(mockUser.getUsername());

        assertNotNull(token);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(extractedUsername, mockUser.getUsername());
    }

    @Test
    @DisplayName("Should validate the token for correct username.")
    void testValidateToken() {
        String token = jwtService.generateToken(mockUser.getUsername());
        assertTrue(jwtService.validateToken(token, mockUserDetails));
    }

    @Test
    @DisplayName("Should not validate the token for different username.")
    void testValidateTokenDifferentUsername() {
        String token = jwtService.generateToken("differentUsername");
        assertFalse(jwtService.validateToken(token, mockUserDetails));
    }

    @Test
    @DisplayName("Should raise exception if token is expired.")
    void testExceptionIfTokenIsExpired() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", Duration.ofMillis(1));
        String token = jwtService.generateToken(mockUser.getUsername());

        Thread.sleep(10);

        assertThrows(ExpiredJwtException.class, () -> jwtService.validateToken(token, mockUserDetails));
    }

    @Test
    @DisplayName("Refresh token should have a different (longer) expiration")
    void shouldGenerateRefreshToken() {
        String token = jwtService.generateRefreshToken(mockUser.getUsername());
        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals(mockUser.getUsername(), username);
    }
}
