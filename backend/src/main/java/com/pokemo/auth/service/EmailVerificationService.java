package com.pokemo.auth.service;

import com.pokemo.auth.domain.EmailVerification;
import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.repository.EmailVerificationRepository;
import com.pokemo.auth.repository.UserAccountRepository;
import com.pokemo.common.ApiException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {

  private static final Duration CODE_TTL = Duration.ofMinutes(10);

  private final EmailVerificationRepository emailVerificationRepository;
  private final UserAccountRepository userAccountRepository;
  private final EmailSender emailSender;
  private final SecureRandom random = new SecureRandom();

  public EmailVerificationService(
      EmailVerificationRepository emailVerificationRepository,
      UserAccountRepository userAccountRepository,
      EmailSender emailSender
  ) {
    this.emailVerificationRepository = emailVerificationRepository;
    this.userAccountRepository = userAccountRepository;
    this.emailSender = emailSender;
  }

  @Transactional
  public void requestCode(String email) {
    String normalized = normalizeEmail(email);
    String code = generateCode();
    emailVerificationRepository.save(
        new EmailVerification(normalized, code, OffsetDateTime.now().plus(CODE_TTL)));
    emailSender.sendVerificationCode(normalized, code);
  }

  @Transactional
  public void verify(String email, String code) {
    String normalized = normalizeEmail(email);
    EmailVerification verification = emailVerificationRepository
        .findFirstByEmailAndConsumedFalseOrderByIdDesc(normalized)
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "인증 요청을 찾을 수 없습니다"));

    if (!verification.matches(code, OffsetDateTime.now())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "인증 코드가 올바르지 않거나 만료되었습니다");
    }

    verification.consume();
    userAccountRepository.findByEmail(normalized).ifPresent(UserAccount::markEmailVerified);
  }

  private String generateCode() {
    return String.format("%06d", random.nextInt(1_000_000));
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase();
  }
}
