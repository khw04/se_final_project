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

  @Column(nullable = false, length = 100)
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

  public boolean emailVerified() {
    return emailVerified;
  }

  public void markEmailVerified() {
    this.emailVerified = true;
  }

  public OffsetDateTime createdAt() {
    return createdAt;
  }
}
