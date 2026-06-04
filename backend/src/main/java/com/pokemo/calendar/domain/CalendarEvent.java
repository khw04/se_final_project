package com.pokemo.calendar.domain;

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
@Table(name = "calendar_events")
public class CalendarEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false, length = 200)
  private String title;

  private Long subjectId;

  @Column(nullable = false)
  private OffsetDateTime startAt;

  private OffsetDateTime endAt;

  @Column(nullable = false)
  private boolean allDay;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CalendarEventType type;

  @Column(length = 20)
  private String recurrenceFreq;

  private Integer recurrenceInterval;

  @Column(length = 50)
  private String recurrenceByweekday;

  @Column(length = 10)
  private String recurrenceEndMode;

  private Integer recurrenceEndCount;

  @Column(length = 20)
  private String recurrenceEndUntil;

  @Column(length = 10)
  private String reminder;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  protected CalendarEvent() {
  }

  public CalendarEvent(
      Long userId,
      String title,
      Long subjectId,
      OffsetDateTime startAt,
      OffsetDateTime endAt,
      boolean allDay,
      CalendarEventType type,
      String recurrenceFreq,
      Integer recurrenceInterval,
      String recurrenceByweekday,
      String recurrenceEndMode,
      Integer recurrenceEndCount,
      String recurrenceEndUntil,
      String reminder
  ) {
    this.userId = userId;
    this.title = title;
    this.subjectId = subjectId;
    this.startAt = startAt;
    this.endAt = endAt;
    this.allDay = allDay;
    this.type = type;
    this.recurrenceFreq = recurrenceFreq;
    this.recurrenceInterval = recurrenceInterval;
    this.recurrenceByweekday = recurrenceByweekday;
    this.recurrenceEndMode = recurrenceEndMode;
    this.recurrenceEndCount = recurrenceEndCount;
    this.recurrenceEndUntil = recurrenceEndUntil;
    this.reminder = reminder;
    this.createdAt = OffsetDateTime.now();
  }

  public void update(
      String title,
      Long subjectId,
      OffsetDateTime startAt,
      OffsetDateTime endAt,
      boolean allDay,
      CalendarEventType type,
      String recurrenceFreq,
      Integer recurrenceInterval,
      String recurrenceByweekday,
      String recurrenceEndMode,
      Integer recurrenceEndCount,
      String recurrenceEndUntil,
      String reminder
  ) {
    this.title = title;
    this.subjectId = subjectId;
    this.startAt = startAt;
    this.endAt = endAt;
    this.allDay = allDay;
    this.type = type;
    this.recurrenceFreq = recurrenceFreq;
    this.recurrenceInterval = recurrenceInterval;
    this.recurrenceByweekday = recurrenceByweekday;
    this.recurrenceEndMode = recurrenceEndMode;
    this.recurrenceEndCount = recurrenceEndCount;
    this.recurrenceEndUntil = recurrenceEndUntil;
    this.reminder = reminder;
  }

  public Long id() { return id; }
  public Long userId() { return userId; }
  public String title() { return title; }
  public Long subjectId() { return subjectId; }
  public OffsetDateTime startAt() { return startAt; }
  public OffsetDateTime endAt() { return endAt; }
  public boolean allDay() { return allDay; }
  public CalendarEventType type() { return type; }
  public String recurrenceFreq() { return recurrenceFreq; }
  public Integer recurrenceInterval() { return recurrenceInterval; }
  public String recurrenceByweekday() { return recurrenceByweekday; }
  public String recurrenceEndMode() { return recurrenceEndMode; }
  public Integer recurrenceEndCount() { return recurrenceEndCount; }
  public String recurrenceEndUntil() { return recurrenceEndUntil; }
  public String reminder() { return reminder; }
  public OffsetDateTime createdAt() { return createdAt; }
}
