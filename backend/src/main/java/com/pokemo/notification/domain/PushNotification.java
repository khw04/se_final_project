package com.pokemo.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "push_notifications")
public class PushNotification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long calendarEventId;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 500)
  private String body;

  @Column(nullable = false)
  private OffsetDateTime sentAt;

  protected PushNotification() {
  }

  public PushNotification(Long userId, Long calendarEventId, String title, String body) {
    this.userId = userId;
    this.calendarEventId = calendarEventId;
    this.title = title;
    this.body = body;
    this.sentAt = OffsetDateTime.now();
  }

  public Long id() { return id; }
  public Long userId() { return userId; }
  public Long calendarEventId() { return calendarEventId; }
  public String title() { return title; }
  public String body() { return body; }
  public OffsetDateTime sentAt() { return sentAt; }
}
