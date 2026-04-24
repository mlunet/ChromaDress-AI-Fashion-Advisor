package com.app.ChromaDress.auth;

import com.app.ChromaDress.user.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {

    @Mock
    JwtService jwtService;
    @Mock
    JpaUserDetailsService userDetailsService;
    @Mock
    FilterChain filterChain;
    @InjectMocks
    JwtFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        User mockUser = new User(1L, "testUser", "password", "email", Collections.emptyList());
        mockUserDetails = new UserPrincipal(mockUser);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should set SecurityContext when token is valid")
    void shouldSetSecurityContextWhenTokenIsValid() throws ServletException, IOException {
        String token = "valid.token.here";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenReturn("testUser");
        when(userDetailsService.loadUserByUsername("testUser")).thenReturn(mockUserDetails);
        when(jwtService.validateToken(token, mockUserDetails)).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testUser", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip filter when Authorization header is missing")
    void shouldSkipFilterWhenNoAuthHeader() throws ServletException, IOException {
        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip filter when Authorization header does not start with Bearer")
    void shouldSkipFilterWhenInvalidAuthHeader() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic someBase64String");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should return 401 JSON when token is expired")
    void shouldReturn401WhenTokenIsExpired() throws ServletException, IOException {
        String token = "expired.token.here";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenThrow(new ExpiredJwtException(null, null, "Expired"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("\"type\": \"TokenExpiredException\""));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should return 401 JSON when token is invalid or malformed")
    void shouldReturn401WhenTokenIsInvalid() throws ServletException, IOException {
        String token = "invalid.token.here";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenThrow(new MalformedJwtException("Malformed token"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("\"type\": \"InvalidTokenException\""));
        verify(filterChain, never()).doFilter(request, response);
    }
}
