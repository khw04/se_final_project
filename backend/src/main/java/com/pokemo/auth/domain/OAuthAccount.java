package com.pokemo.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "oauth_accounts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_oauth_accounts_provider_user", columnNames = {"provider", "provider_user_id"})
})
public class OAuthAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserAccount user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OAuthProvider provider;

  @Column(name = "provider_user_id", nullable = false, length = 255)
  private String providerUserId;

  @Column(length = 320)
  private String email;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  protected OAuthAccount() {
  }

  public OAuthAccount(UserAccount user, OAuthProvider provider, String providerUserId, String email) {
    this.user = user;
    this.provider = provider;
    this.providerUserId = providerUserId;
    this.email = email;
    this.createdAt = OffsetDateTime.now();
  }

  public Long id() {
    return id;
  }

  public UserAccount user() {
    return user;
  }

  public OAuthProvider provider() {
    return provider;
  }

  public String providerUserId() {
    return providerUserId;
  }

  public String email() {
    return email;
  }

  public OffsetDateTime createdAt() {
    return createdAt;
  }
}
