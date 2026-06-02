package com.pokemo.subject.api;

import com.pokemo.subject.domain.Subject;

public record SubjectResponse(Long id, String name, String color) {

  public static SubjectResponse from(Subject subject) {
    return new SubjectResponse(subject.id(), subject.name(), subject.color());
  }
}
