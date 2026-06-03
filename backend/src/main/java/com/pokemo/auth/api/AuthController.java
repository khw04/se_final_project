package com.pokemo.auth.api;

import com.pokemo.auth.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  UserResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/refresh")
  AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
    return authService.refresh(request);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void logout(@Valid @RequestBody LogoutRequest request) {
    authService.logout(request.refreshToken());
  }

  @GetMapping("/me")
  UserResponse me(Principal principal) {
    return authService.currentUser(principal.getName());
  }
}
