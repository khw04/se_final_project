package com.pokemo.auth.service;

import com.pokemo.auth.domain.PasswordResetToken;
import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.repository.PasswordResetTokenRepository;
import com.pokemo.auth.repository.UserAccountRepository;
import com.pokemo.common.ApiException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

  private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final UserAccountRepository userAccountRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailSender emailSender;

  public PasswordResetService(
      PasswordResetTokenRepository passwordResetTokenRepository,
      UserAccountRepository userAccountRepository,
      PasswordEncoder passwordEncoder,
      EmailSender emailSender
  ) {
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.userAccountRepository = userAccountRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailSender = emailSender;
  }

  /**
   * 재설정 토큰 생성. 계정 존재 여부를 노출하지 않기 위해 사용자가 없어도 정상 응답한다.
   */
  @Transactional
  public void requestReset(String email) {
    String normalized = normalizeEmail(email);
    userAccountRepository.findByEmail(normalized).ifPresent(user -> {
      String token = UUID.randomUUID().toString();
      passwordResetTokenRepository.save(
          new PasswordResetToken(normalized, token, OffsetDateTime.now().plus(TOKEN_TTL)));
      emailSender.sendPasswordResetToken(normalized, token);
    });
  }

  @Transactional(readOnly = true)
  public void validateToken(String token) {
    loadUsableToken(token);
  }

  @Transactional
  public void resetPassword(String token, String newPassword) {
    PasswordResetToken resetToken = loadUsableToken(token);
    UserAccount user = userAccountRepository.findByEmail(resetToken.email())
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "더 이상 존재하지 않는 계정입니다"));

    user.changePassword(passwordEncoder.encode(newPassword));
    resetToken.consume();
  }

  private PasswordResetToken loadUsableToken(String token) {
    PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "유효하지 않은 재설정 토큰입니다"));

    if (!resetToken.usableAt(OffsetDateTime.now())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "만료되었거나 이미 사용된 재설정 토큰입니다");
    }

    return resetToken;
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase();
  }
}
