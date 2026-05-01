package com.app.ChromaDress.core.utils;

import com.app.ChromaDress.core.exception.ResourceNotFoundException;
import com.app.ChromaDress.user.User;
import com.app.ChromaDress.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

  private final UserRepository userRepository;

  public User getUserPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResourceNotFoundException("User is not logged in or session is expired.");
    }

    String username = authentication.getName();
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found."));
  }
}
