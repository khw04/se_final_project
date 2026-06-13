package com.pokemo.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 개발/시연용 메일 발송 구현. 실제 발송 대신 콘솔 로그로 코드를 출력한다.
 * pokemo.mail.enabled=true 가 아닐 때(기본값) 사용된다.
 */
@Component
@ConditionalOnProperty(prefix = "pokemo.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class ConsoleEmailSender implements EmailSender {

  private static final Logger log = LoggerFactory.getLogger(ConsoleEmailSender.class);

  @Override
  public void sendVerificationCode(String email, String code) {
    log.info("[이메일 인증] 수신자={} 인증코드={}", email, code);
  }

  @Override
  public void sendPasswordResetToken(String email, String token) {
    log.info("[비밀번호 재설정] 수신자={} 재설정토큰={}", email, token);
  }
}
