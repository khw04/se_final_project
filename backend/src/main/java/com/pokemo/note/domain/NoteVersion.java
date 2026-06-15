package com.pokemo.note.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "note_versions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_note_versions_note_version", columnNames = {"note_id", "version_number"})
}, indexes = {
    @Index(name = "idx_note_versions_note_created", columnList = "note_id, created_at")
})
public class NoteVersion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long noteId;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private int versionNumber;

  @Column(nullable = false, length = 200)
  private String title;

  private Long subjectId;

  @Lob
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  protected NoteVersion() {
  }

  public NoteVersion(Long noteId, Long userId, int versionNumber, String title, Long subjectId, String content) {
    this.noteId = noteId;
    this.userId = userId;
    this.versionNumber = versionNumber;
    this.title = title;
    this.subjectId = subjectId;
    this.content = content == null ? "" : content;
    this.createdAt = OffsetDateTime.now();
  }

  public Long id() { return id; }
  public Long noteId() { return noteId; }
  public Long userId() { return userId; }
  public int versionNumber() { return versionNumber; }
  public String title() { return title; }
  public Long subjectId() { return subjectId; }
  public String content() { return content; }
  public OffsetDateTime createdAt() { return createdAt; }
}
