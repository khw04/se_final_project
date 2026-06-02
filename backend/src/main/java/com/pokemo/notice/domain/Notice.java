package com.pokemo.notice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notices")
public class Notice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 10000)
  private String body;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NoticeTag tag;

  @Column(nullable = false, length = 320)
  private String author;

  @Column(nullable = false)
  private boolean pinned;

  @Column(nullable = false)
  private long viewCount;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  protected Notice() {
  }

  public Notice(String title, String body, NoticeTag tag, boolean pinned, String author) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Notice title must not be blank");
    }
    if (body == null || body.isBlank()) {
      throw new IllegalArgumentException("Notice body must not be blank");
    }
    if (tag == null) {
      throw new IllegalArgumentException("Notice tag must not be null");
    }
    if (author == null || author.isBlank()) {
      throw new IllegalArgumentException("Notice author must not be blank");
    }

    this.title = title;
    this.body = body;
    this.tag = tag;
    this.pinned = pinned;
    this.author = author;
    this.viewCount = 0;
    OffsetDateTime now = OffsetDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void update(String title, String body, NoticeTag tag, boolean pinned) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Notice title must not be blank");
    }
    if (body == null || body.isBlank()) {
      throw new IllegalArgumentException("Notice body must not be blank");
    }
    if (tag == null) {
      throw new IllegalArgumentException("Notice tag must not be null");
    }

    this.title = title;
    this.body = body;
    this.tag = tag;
    this.pinned = pinned;
    this.updatedAt = OffsetDateTime.now();
  }

  public void increaseViewCount() {
    this.viewCount += 1;
  }

  public Long id() {
    return id;
  }

  public String title() {
    return title;
  }

  public String body() {
    return body;
  }

  public NoticeTag tag() {
    return tag;
  }

  public String author() {
    return author;
  }

  public boolean pinned() {
    return pinned;
  }

  public long viewCount() {
    return viewCount;
  }

  public OffsetDateTime createdAt() {
    return createdAt;
  }

  public OffsetDateTime updatedAt() {
    return updatedAt;
  }
}
