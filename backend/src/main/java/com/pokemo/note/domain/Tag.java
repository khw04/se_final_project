package com.pokemo.note.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "tags", uniqueConstraints = {
    @UniqueConstraint(name = "uk_tags_user_name", columnNames = {"userId", "name"})
})
public class Tag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false, length = 50)
  private String name;

  protected Tag() {
  }

  public Tag(Long userId, String name) {
    this.userId = userId;
    this.name = name;
  }

  public Long id() { return id; }
  public Long userId() { return userId; }
  public String name() { return name; }
}
