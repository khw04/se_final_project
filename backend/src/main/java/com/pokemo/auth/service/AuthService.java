package com.pokemo.auth.service;

import com.pokemo.auth.api.AuthResponse;
import com.pokemo.auth.api.LoginRequest;
import com.pokemo.auth.api.RefreshTokenRequest;
import com.pokemo.auth.api.RegisterRequest;
import com.pokemo.auth.api.UserResponse;
import com.pokemo.auth.domain.AuthToken;
import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.domain.UserRole;
import com.pokemo.auth.repository.AuthTokenRepository;
import com.pokemo.auth.repository.UserAccountRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserAccountRepository userAccountRepository;
  private final AuthTokenRepository authTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenService jwtTokenService;

  public AuthService(
      UserAccountRepository userAccountRepository,
      AuthTokenRepository authTokenRepository,
      PasswordEncoder passwordEncoder,
      JwtTokenService jwtTokenService
  ) {
    this.userAccountRepository = userAccountRepository;
    this.authTokenRepository = authTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenService = jwtTokenService;
  }

  @Transactional
  public UserResponse register(RegisterRequest request) {
    String email = normalizeEmail(request.email());
    if (userAccountRepository.existsByEmail(email)) {
      throw new AuthException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다");
    }

    UserAccount user = new UserAccount(email, passwordEncoder.encode(request.password()), UserRole.USER);
    return UserResponse.from(userAccountRepository.save(user));
  }

  @Transactional
  public AuthResponse login(LoginRequest request) {
    String email = normalizeEmail(request.email());
    UserAccount user = userAccountRepository.findByEmail(email)
        .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "가입되지 않은 이메일입니다"));

    if (user.passwordHash() == null || !passwordEncoder.matches(request.password(), user.passwordHash())) {
      throw new AuthException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다");
    }

    return issueTokens(user);
  }

  /**
   * Access/Refresh 토큰을 발급하고 Refresh 토큰을 저장한다.
   * 이메일/비밀번호 로그인과 소셜 로그인이 동일한 응답을 만들도록 공유한다.
   */
  @Transactional
  public AuthResponse issueTokens(UserAccount user) {
    String accessToken = jwtTokenService.createAccessToken(user);
    String refreshToken = UUID.randomUUID().toString();
    OffsetDateTime refreshExpiresAt = OffsetDateTime.now().plus(jwtTokenService.refreshTokenTtl());
    authTokenRepository.save(new AuthToken(user, refreshToken, refreshExpiresAt));

    return new AuthResponse(
        user.id(),
        user.email(),
        user.role().name(),
        accessToken,
        refreshToken,
        "Bearer",
        jwtTokenService.accessTokenTtl().toSeconds()
    );
  }

  @Transactional
  public AuthResponse refresh(RefreshTokenRequest request) {
    AuthToken refreshToken = authTokenRepository.findByToken(request.refreshToken())
        .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다"));

    if (!refreshToken.activeAt(OffsetDateTime.now())) {
      throw new AuthException(HttpStatus.UNAUTHORIZED, "만료된 리프레시 토큰입니다");
    }

    UserAccount user = refreshToken.user();
    return new AuthResponse(
        user.id(),
        user.email(),
        user.role().name(),
        jwtTokenService.createAccessToken(user),
        refreshToken.token(),
        "Bearer",
        jwtTokenService.accessTokenTtl().toSeconds()
    );
  }

  @Transactional
  public void logout(String refreshToken) {
    authTokenRepository.findByToken(refreshToken).ifPresent(AuthToken::revoke);
  }

  @Transactional(readOnly = true)
  public UserResponse currentUser(String email) {
    UserAccount user = userAccountRepository.findByEmail(normalizeEmail(email))
        .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"));
    return UserResponse.from(user);
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase();
  }
}
