package com.pokemo.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "push_subscriptions")
public class PushSubscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false, unique = true, length = 500)
  private String endpoint;

  @Column(nullable = false, length = 200)
  private String p256dh;

  @Column(nullable = false, length = 200)
  private String auth;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  protected PushSubscription() {
  }

  public PushSubscription(Long userId, String endpoint, String p256dh, String auth) {
    this.userId = userId;
    this.endpoint = endpoint;
    this.p256dh = p256dh;
    this.auth = auth;
    this.createdAt = OffsetDateTime.now();
  }

  public Long id() { return id; }
  public Long userId() { return userId; }
  public String endpoint() { return endpoint; }
  public String p256dh() { return p256dh; }
  public String auth() { return auth; }
  public OffsetDateTime createdAt() { return createdAt; }
}
