package com.app.ChromaDress.auth;

import com.app.ChromaDress.core.exception.InvalidCredentialsException;
import com.app.ChromaDress.core.exception.UserAlreadyExistsException;
import com.app.ChromaDress.core.utils.SecurityUtils;
import com.app.ChromaDress.user.User;
import com.app.ChromaDress.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityUtils securityUtils;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDTO registerUser(RegistrationDTO dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            throw new UserAlreadyExistsException("Username is already in use.");
        }

        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already in use.");
        }

        User newUser = new User();
        newUser.setUsername(dto.username());

        String encodedPassword = passwordEncoder.encode(dto.password());
        newUser.setPassword(encodedPassword);

        newUser.setEmail(dto.email());
        userRepository.save(newUser);

        String accessToken = jwtService.generateToken(newUser.getUsername());
        String refreshToken = createAndSaveRefreshToken(newUser);
        return new AuthResponseDTO(accessToken, refreshToken, "Registration successful.");
    }

    @Transactional
    public AuthResponseDTO login(LoginDTO dto) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        User user = principal.getUser();

        String accessToken = jwtService.generateToken(user.getUsername());
        String refreshToken = createAndSaveRefreshToken(user);
        return new AuthResponseDTO(accessToken, refreshToken, "Login successful.");
    }

    @Transactional
    public AuthResponseDTO refresh(RefreshTokenDTO dto) {

        RefreshToken storedToken = refreshTokenRepository.findByToken(dto.refreshToken())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token."));

        if (storedToken.isRevoked()) {
            refreshTokenRepository.deleteByUser(storedToken.getUser());
            throw new InvalidCredentialsException("Refresh token already used. Please login again.");
        }
        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new InvalidCredentialsException("Refresh token expired. Please login again.");
        }

        User user = storedToken.getUser();
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtService.generateToken(user.getUsername());
        String newRefreshToken = createAndSaveRefreshToken(user);
        return new AuthResponseDTO(newAccessToken, newRefreshToken, "Token successfully refreshed.");
    }

    @Transactional
    public void logout() {
        User user = securityUtils.getUserPrincipal();
        refreshTokenRepository.deleteByUser(user);
    }

    private String createAndSaveRefreshToken(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        String token = jwtService.generateRefreshToken(user.getUsername());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
        return token;
    }
}
