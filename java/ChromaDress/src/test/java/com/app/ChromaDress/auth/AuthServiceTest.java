package com.app.ChromaDress.auth;

import com.app.ChromaDress.core.exception.InvalidCredentialsException;
import com.app.ChromaDress.core.exception.UserAlreadyExistsException;
import com.app.ChromaDress.core.utils.SecurityUtils;
import com.app.ChromaDress.user.User;
import com.app.ChromaDress.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUsername("username");
        mockUser.setPassword("password");
        mockUser.setEmail("email");
    }

    @Test
    @DisplayName("Should register new user successfully.")
    void shouldRegisterNewUser() {
        RegistrationDTO dto = new RegistrationDTO("newUser", "newPassword", "newEmail");
        when(userRepository.findByUsername(dto.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");
        when(jwtService.generateToken(anyString())).thenReturn("mockAccessToken");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("mockRefreshToken");

        AuthResponseDTO response = authService.registerUser(dto);

        assertNotNull(response);
        assertEquals("mockAccessToken", response.accessToken());
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException if username is taken.")
    void shouldThrowUserAlreadyExistsException() {
        RegistrationDTO dto = new RegistrationDTO("username", "newPassword", "newEmail");
        when(userRepository.findByUsername(dto.username())).thenReturn(Optional.of(mockUser));

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully.")
    void shouldLoginSuccessfully() {
        LoginDTO dto = new LoginDTO("username", "password");
        Authentication authentication = mock(Authentication.class);
        UserPrincipal principal = new UserPrincipal(mockUser);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtService.generateToken(dto.username())).thenReturn("mockAccessToken");
        when(jwtService.generateRefreshToken(dto.username())).thenReturn("mockRefreshToken");

        AuthResponseDTO response = authService.login(dto);

        assertNotNull(response);
        assertEquals("mockAccessToken", response.accessToken());
        assertEquals("mockRefreshToken", response.refreshToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should refresh token successfully.")
    void shouldRefreshTokenSuccessfully() {
        RefreshTokenDTO dto = new RefreshTokenDTO("validRefresh");
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(mockUser);
        storedToken.setToken("validRefresh");
        storedToken.setRevoked(false);
        storedToken.setExpiryDate(Instant.now().plus(1, ChronoUnit.DAYS));

        when(refreshTokenRepository.findByToken(dto.refreshToken())).thenReturn(Optional.of(storedToken));
        when(jwtService.generateToken("username")).thenReturn("mockAccessToken");
        when(jwtService.generateRefreshToken("username")).thenReturn("mockRefreshToken");

        AuthResponseDTO response = authService.refresh(dto);

        assertNotNull(response);
        assertEquals("mockAccessToken", response.accessToken());
        assertTrue(storedToken.isRevoked());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw exception and delete tokens if refresh token is revoked.")
    void shouldThrowExceptionAndDeleteTokensIfRefreshTokenIsRevoked() {
        RefreshTokenDTO dto = new RefreshTokenDTO("revokedToken");
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(mockUser);
        storedToken.setRevoked(true);

        when(refreshTokenRepository.findByToken("revokedToken")).thenReturn(Optional.of(storedToken));

        assertThrows(InvalidCredentialsException.class, () -> authService.refresh(dto));
        verify(refreshTokenRepository).deleteByUser(mockUser);
    }


}
