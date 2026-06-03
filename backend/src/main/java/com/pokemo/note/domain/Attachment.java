package com.pokemo.note.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long noteId;

  @Column(nullable = false, length = 255)
  private String fileName;

  @Column(nullable = false, length = 255)
  private String storedName;

  @Column(nullable = false, length = 100)
  private String mimeType;

  @Column(nullable = false)
  private Long size;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  protected Attachment() {}

  public Attachment(Long noteId, String fileName, String storedName, String mimeType, Long size) {
    this.noteId = noteId;
    this.fileName = fileName;
    this.storedName = storedName;
    this.mimeType = mimeType;
    this.size = size;
    this.createdAt = OffsetDateTime.now();
  }

  public Long id() { return id; }
  public Long noteId() { return noteId; }
  public String fileName() { return fileName; }
  public String storedName() { return storedName; }
  public String mimeType() { return mimeType; }
  public Long size() { return size; }
  public OffsetDateTime createdAt() { return createdAt; }
}
