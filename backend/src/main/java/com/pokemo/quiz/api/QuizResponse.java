package com.pokemo.quiz.api;

import java.util.List;

public record QuizResponse(
    long id,
    String title,
    long subjectId,
    String generatedBy,
    Long generatedFromNoteId,
    List<Long> questionIds,
    List<QuestionResponse> questions,
    String createdAt
) {}
