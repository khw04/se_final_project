package com.pokemo.quiz.api;

import java.util.List;

public record QuestionResponse(
    long id,
    String type,
    String text,
    List<String> choices,
    Integer correctIndex,
    String correctText,
    Boolean correctBool,
    String explanation,
    String difficulty,
    long subjectId,
    List<String> conceptTags,
    String createdAt
) {}
