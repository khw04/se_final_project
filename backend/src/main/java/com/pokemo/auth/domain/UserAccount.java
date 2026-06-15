package com.pokemo.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_accounts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_accounts_email", columnNames = "email")
})
public class UserAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 320)
  private String email;

  @Column(length = 100)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole role;

  @Column(nullable = false)
  private boolean emailVerified;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  protected UserAccount() {
  }

  public UserAccount(String email, String passwordHash, UserRole role) {
    this.email = email;
    this.passwordHash = passwordHash;
    this.role = role;
    this.emailVerified = false;
    this.createdAt = OffsetDateTime.now();
  }

  /**
   * 소셜 로그인 전용 계정 생성 팩토리. 비밀번호 없이 가입되며,
   * provider가 이메일 검증을 보장한 경우에만 emailVerified를 true로 둔다.
   */
  public static UserAccount oauthUser(String email, UserRole role, boolean emailVerified) {
    UserAccount user = new UserAccount();
    user.email = email;
    user.passwordHash = null;
    user.role = role;
    user.emailVerified = emailVerified;
    user.createdAt = OffsetDateTime.now();
    return user;
  }

  public Long id() {
    return id;
  }

  public String email() {
    return email;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public UserRole role() {
    return role;
  }

  public void promoteToAdmin() {
    this.role = UserRole.ADMIN;
  }

  public boolean emailVerified() {
    return emailVerified;
  }

  public void markEmailVerified() {
    this.emailVerified = true;
  }

  public void changePassword(String passwordHash) {
    if (passwordHash == null || passwordHash.isBlank()) {
      throw new IllegalArgumentException("Password hash must not be blank");
    }
    this.passwordHash = passwordHash;
  }

  public OffsetDateTime createdAt() {
    return createdAt;
  }
}
