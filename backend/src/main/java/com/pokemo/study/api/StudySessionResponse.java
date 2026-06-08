package com.pokemo.study.api;

import com.pokemo.study.domain.StudySession;

public record StudySessionResponse(
    Long id,
    Long subjectId,
    String startedAt,
    String endedAt,
    int studySeconds
) {
  public static StudySessionResponse from(StudySession session) {
    return new StudySessionResponse(
        session.id(),
        session.subjectId(),
        session.startedAt().toString(),
        session.endedAt().toString(),
        session.studySeconds()
    );
  }
}
