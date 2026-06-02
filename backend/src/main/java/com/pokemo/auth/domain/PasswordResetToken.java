package com.pokemo.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "password_reset_tokens", uniqueConstraints = {
    @UniqueConstraint(name = "uk_password_reset_tokens_token", columnNames = "token")
})
public class PasswordResetToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 320)
  private String email;

  @Column(nullable = false, length = 64)
  private String token;

  @Column(nullable = false)
  private OffsetDateTime expiresAt;

  @Column(nullable = false)
  private boolean consumed;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  protected PasswordResetToken() {
  }

  public PasswordResetToken(String email, String token, OffsetDateTime expiresAt) {
    this.email = email;
    this.token = token;
    this.expiresAt = expiresAt;
    this.consumed = false;
    this.createdAt = OffsetDateTime.now();
  }

  public boolean usableAt(OffsetDateTime now) {
    return !consumed && expiresAt.isAfter(now);
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

  public String token() {
    return token;
  }

  public OffsetDateTime expiresAt() {
    return expiresAt;
  }

  public boolean consumed() {
    return consumed;
  }
}
