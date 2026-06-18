package com.pokemo.common;

/**
 * 보안 감사 로그 등에 개인정보가 평문으로 남지 않도록 값을 마스킹한다.
 * 추적용 식별은 내부 userId로 하고, 이메일 같은 PII는 마스킹한 형태로만 기록한다.
 */
public final class LogMask {

  private LogMask() {
  }

  /** 이메일을 첫 글자 + 도메인만 남기고 마스킹한다. 예: {@code student@example.com -> s***@example.com} */
  public static String email(String email) {
    if (email == null || email.isBlank()) {
      return "(none)";
    }
    int at = email.indexOf('@');
    if (at <= 0) {
      return "***";
    }
    return email.charAt(0) + "***" + email.substring(at);
  }
}
