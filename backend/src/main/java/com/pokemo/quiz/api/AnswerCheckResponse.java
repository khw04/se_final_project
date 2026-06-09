package com.pokemo.quiz.api;

public record AnswerCheckResponse(
    long questionId,
    boolean correct,
    Integer correctIndex,
    String correctText,
    Boolean correctBool,
    String explanation
) {}
