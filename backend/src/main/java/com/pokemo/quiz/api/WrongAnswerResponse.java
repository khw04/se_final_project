package com.pokemo.quiz.api;

public record WrongAnswerResponse(
    long questionId,
    QuestionResponse question,
    int missCount,
    Long lastAttemptId,
    String lastMissedAt,
    String concept
) {}
