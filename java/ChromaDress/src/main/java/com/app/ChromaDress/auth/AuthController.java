package com.app.ChromaDress.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegistrationDTO dto) {
    return ResponseEntity.ok(authService.registerUser(dto));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
    return ResponseEntity.ok(authService.login(dto));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponseDTO> refreshJwt(@NotNull @RequestBody RefreshTokenDTO dto) {
    return ResponseEntity.ok(authService.refresh(dto));
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout() {
    authService.logout();
    return ResponseEntity.ok("Logout successful.");
  }

}
