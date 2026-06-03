package com.pokemo.quiz.api;

public record AttemptResponse(
    long id,
    long quizId,
    int totalQuestions,
    int correctCount,
    double accuracy,
    String startedAt,
    String completedAt
) {}
