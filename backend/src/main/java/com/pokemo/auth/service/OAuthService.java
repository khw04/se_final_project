package com.pokemo.auth.service;

import com.pokemo.auth.api.AuthResponse;
import com.pokemo.auth.api.OAuthLoginRequest;
import com.pokemo.auth.domain.OAuthAccount;
import com.pokemo.auth.domain.OAuthProvider;
import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.domain.UserRole;
import com.pokemo.auth.repository.OAuthAccountRepository;
import com.pokemo.auth.repository.UserAccountRepository;
import com.pokemo.subject.service.SubjectService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuthService {

  private final List<OAuthClient> clients;
  private final UserAccountRepository userAccountRepository;
  private final OAuthAccountRepository oauthAccountRepository;
  private final AuthService authService;
  private final SubjectService subjectService;

  public OAuthService(
      List<OAuthClient> clients,
      UserAccountRepository userAccountRepository,
      OAuthAccountRepository oauthAccountRepository,
      AuthService authService,
      SubjectService subjectService
  ) {
    this.clients = clients;
    this.userAccountRepository = userAccountRepository;
    this.oauthAccountRepository = oauthAccountRepository;
    this.authService = authService;
    this.subjectService = subjectService;
  }

  @Transactional
  public AuthResponse login(OAuthProvider provider, OAuthLoginRequest request) {
    OAuthClient client = clients.stream()
        .filter(candidate -> candidate.provider() == provider)
        .findFirst()
        .orElseThrow(() -> new AuthException(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다"));

    OAuthUserInfo info = client.fetchUser(request.code(), request.redirectUri());
    UserAccount user = resolveUser(provider, info);
    return authService.issueTokens(user);
  }

  private UserAccount resolveUser(OAuthProvider provider, OAuthUserInfo info) {
    Optional<OAuthAccount> linked =
        oauthAccountRepository.findByProviderAndProviderUserId(provider, info.providerUserId());
    if (linked.isPresent()) {
      return linked.get().user();
    }

    String email = normalizeEmail(resolveEmail(provider, info));
    Optional<UserAccount> existing = userAccountRepository.findByEmail(email);

    UserAccount user;
    if (existing.isPresent()) {
      // 같은 이메일이 이미 있으면, provider가 이메일을 검증했을 때만 자동 연결한다.
      if (!info.emailVerified()) {
        throw new AuthException(HttpStatus.CONFLICT,
            "이미 가입된 이메일입니다. 기존 방식으로 로그인한 뒤 계정을 연결해주세요");
      }
      user = existing.get();
    } else {
      user = userAccountRepository.save(
          UserAccount.oauthUser(email, UserRole.USER, info.emailVerified()));
      subjectService.seedDefaults(user.id());
    }

    oauthAccountRepository.save(new OAuthAccount(user, provider, info.providerUserId(), info.email()));
    return user;
  }

  /**
   * provider가 이메일을 주지 않으면(카카오 이메일 미동의 등) provider 식별자로
   * 충돌하지 않는 합성 이메일을 만든다.
   */
  private String resolveEmail(OAuthProvider provider, OAuthUserInfo info) {
    if (info.email() != null && !info.email().isBlank()) {
      return info.email();
    }
    String key = provider.name().toLowerCase();
    return key + "_" + info.providerUserId() + "@" + key + ".local";
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase();
  }
}
