package com.pokemo.auth.service;

/**
 * 인증 메일 발송 추상화. 현재는 콘솔 로그 구현(ConsoleEmailSender)만 제공하며,
 * 추후 실제 SMTP/외부 메일 서비스 구현으로 교체할 수 있다.
 */
public interface EmailSender {

  void sendVerificationCode(String email, String code);

  void sendPasswordResetToken(String email, String token);
}
