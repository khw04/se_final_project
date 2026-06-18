package com.pokemo.subject.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "subjects", uniqueConstraints = {
    @UniqueConstraint(name = "uk_subjects_user_name", columnNames = {"user_id", "name"})
})
public class Subject {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 7)
  private String color;

  protected Subject() {
  }

  public Subject(Long userId, String name, String color) {
    if (userId == null) {
      throw new IllegalArgumentException("Subject owner must not be null");
    }
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Subject name must not be blank");
    }
    if (color == null || color.isBlank()) {
      throw new IllegalArgumentException("Subject color must not be blank");
    }

    this.userId = userId;
    this.name = name;
    this.color = color;
  }

  public void update(String name, String color) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Subject name must not be blank");
    }
    if (color == null || color.isBlank()) {
      throw new IllegalArgumentException("Subject color must not be blank");
    }
    this.name = name;
    this.color = color;
  }

  public Long id() {
    return id;
  }

  public Long userId() {
    return userId;
  }

  public String name() {
    return name;
  }

  public String color() {
    return color;
  }
}
