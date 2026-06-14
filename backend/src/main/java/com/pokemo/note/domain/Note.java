package com.pokemo.note.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notes")
public class Note {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false, length = 200)
  private String title;

  private Long subjectId;

  @Lob
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"))
  @Column(name = "tag_id")
  private List<Long> tagIds = new ArrayList<>();

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  protected Note() {
  }

  public Note(Long userId, String title, Long subjectId, String content) {
    this.userId = userId;
    this.title = title;
    this.subjectId = subjectId;
    this.content = content;
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = OffsetDateTime.now();
  }

  public void updateContent(String content) {
    this.content = content;
    this.updatedAt = OffsetDateTime.now();
  }

  public void updateTitle(String title) {
    this.title = title;
    this.updatedAt = OffsetDateTime.now();
  }

  public void updateSubject(Long subjectId) {
    this.subjectId = subjectId;
    this.updatedAt = OffsetDateTime.now();
  }

  public void update(String title, Long subjectId, String content) {
    this.title = title;
    this.subjectId = subjectId;
    this.content = content;
    this.updatedAt = OffsetDateTime.now();
  }

  public void addTag(Long tagId) {
    if (!tagIds.contains(tagId)) {
      tagIds.add(tagId);
      this.updatedAt = OffsetDateTime.now();
    }
  }

  public void removeTag(Long tagId) {
    boolean removed = false;
    while (tagIds.remove(tagId)) {
      removed = true;
    }
    if (removed) {
      this.updatedAt = OffsetDateTime.now();
    }
  }

  public Long id() { return id; }
  public Long userId() { return userId; }
  public String title() { return title; }
  public Long subjectId() { return subjectId; }
  public String content() { return content; }
  public List<Long> tagIds() { return tagIds; }
  public OffsetDateTime createdAt() { return createdAt; }
  public OffsetDateTime updatedAt() { return updatedAt; }
}
