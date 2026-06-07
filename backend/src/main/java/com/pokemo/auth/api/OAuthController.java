package com.pokemo.auth.api;

import com.pokemo.auth.domain.OAuthProvider;
import com.pokemo.auth.service.AuthException;
import com.pokemo.auth.service.OAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth")
public class OAuthController {

  private final OAuthService oauthService;

  public OAuthController(OAuthService oauthService) {
    this.oauthService = oauthService;
  }

  @PostMapping("/{provider}")
  AuthResponse login(@PathVariable String provider, @Valid @RequestBody OAuthLoginRequest request) {
    return oauthService.login(parseProvider(provider), request);
  }

  private OAuthProvider parseProvider(String provider) {
    try {
      return OAuthProvider.valueOf(provider.trim().toUpperCase());
    } catch (IllegalArgumentException exception) {
      throw new AuthException(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다: " + provider);
    }
  }
}
