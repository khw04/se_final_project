package com.pokemo.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 320)
  private String email;

  @Column(nullable = false, length = 6)
  private String code;

  @Column(nullable = false)
  private OffsetDateTime expiresAt;

  @Column(nullable = false)
  private boolean consumed;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  protected EmailVerification() {
  }

  public EmailVerification(String email, String code, OffsetDateTime expiresAt) {
    this.email = email;
    this.code = code;
    this.expiresAt = expiresAt;
    this.consumed = false;
    this.createdAt = OffsetDateTime.now();
  }

  public boolean matches(String code, OffsetDateTime now) {
    return !consumed && this.code.equals(code) && expiresAt.isAfter(now);
  }

  public void consume() {
    this.consumed = true;
  }

  public Long id() {
    return id;
  }

  public String email() {
    return email;
  }

  public String code() {
    return code;
  }

  public OffsetDateTime expiresAt() {
    return expiresAt;
  }

  public boolean consumed() {
    return consumed;
  }
}
