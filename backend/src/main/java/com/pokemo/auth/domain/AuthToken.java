package com.pokemo.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "auth_tokens", uniqueConstraints = {
    @UniqueConstraint(name = "uk_auth_tokens_token", columnNames = "token")
})
public class AuthToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserAccount user;

  @Column(nullable = false, length = 512)
  private String token;

  @Column(nullable = false)
  private OffsetDateTime expiresAt;

  @Column(nullable = false)
  private boolean revoked;

  protected AuthToken() {
  }

  public AuthToken(UserAccount user, String token, OffsetDateTime expiresAt) {
    this.user = user;
    this.token = token;
    this.expiresAt = expiresAt;
    this.revoked = false;
  }

  public UserAccount user() {
    return user;
  }

  public String token() {
    return token;
  }

  public OffsetDateTime expiresAt() {
    return expiresAt;
  }

  public boolean revoked() {
    return revoked;
  }

  public boolean activeAt(OffsetDateTime now) {
    return !revoked && expiresAt.isAfter(now);
  }

  public void revoke() {
    this.revoked = true;
  }
}
