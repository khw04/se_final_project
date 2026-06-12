package com.pokemo.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * SMTP를 통해 실제로 이메일을 발송하는 구현. pokemo.mail.enabled=true 일 때만 활성화된다.
 */
@Component
@ConditionalOnProperty(prefix = "pokemo.mail", name = "enabled", havingValue = "true")
public class SmtpEmailSender implements EmailSender {

  private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

  private final JavaMailSender mailSender;
  private final String from;

  public SmtpEmailSender(JavaMailSender mailSender, @Value("${spring.mail.username}") String from) {
    this.mailSender = mailSender;
    this.from = from;
  }

  @Override
  public void sendVerificationCode(String email, String code) {
    send(email, "[Pokemo] 이메일 인증 코드",
        "안녕하세요, Pokemo입니다.\n\n인증 코드: " + code + "\n\n이 코드는 10분간 유효합니다.");
  }

  @Override
  public void sendPasswordResetToken(String email, String token) {
    send(email, "[Pokemo] 비밀번호 재설정",
        "안녕하세요, Pokemo입니다.\n\n비밀번호 재설정 코드: " + token + "\n\n잠시 후 만료됩니다.");
  }

  private void send(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    try {
      mailSender.send(message);
    } catch (Exception e) {
      log.warn("이메일 발송 실패: to={}, error={}", to, e.getMessage());
      throw new IllegalStateException("이메일 발송에 실패했습니다", e);
    }
  }
}
