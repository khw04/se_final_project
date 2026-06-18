package com.pokemo.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * 로그인 자격 증명(비밀번호 해시)을 사용자 프로필({@link UserAccount})과 분리해 보관한다.
 * 설계문서의 Credential 모델 분리 의도를 반영한 것으로, 소셜 전용 계정은 Credential 없이 존재할 수 있다.
 */
@Entity
@Table(name = "credentials")
public class Credential {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  protected Credential() {
  }

  public Credential(String passwordHash) {
    changePassword(passwordHash);
  }

  public Long id() {
    return id;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public void changePassword(String passwordHash) {
    if (passwordHash == null || passwordHash.isBlank()) {
      throw new IllegalArgumentException("Password hash must not be blank");
    }
    this.passwordHash = passwordHash;
    this.updatedAt = OffsetDateTime.now();
  }

  public OffsetDateTime updatedAt() {
    return updatedAt;
  }
}
