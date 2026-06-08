package com.pokemo.study.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "study_sessions")
public class StudySession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long subjectId;

  @Column(nullable = false)
  private OffsetDateTime startedAt;

  @Column(nullable = false)
  private OffsetDateTime endedAt;

  @Column(name = "study_minutes", nullable = false)
  private int studySeconds;

  protected StudySession() {
  }

  public StudySession(Long userId, Long subjectId, OffsetDateTime startedAt, OffsetDateTime endedAt, int studySeconds) {
    this.userId = userId;
    this.subjectId = subjectId;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.studySeconds = studySeconds;
  }

  public Long id() { return id; }
  public Long userId() { return userId; }
  public Long subjectId() { return subjectId; }
  public OffsetDateTime startedAt() { return startedAt; }
  public OffsetDateTime endedAt() { return endedAt; }
  public int studySeconds() { return studySeconds; }
}
