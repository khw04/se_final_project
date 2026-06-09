package com.pokemo.quiz.api;

public record WrongAnswerResponse(
    long questionId,
    QuestionResponse question,
    int missCount,
    Long lastAttemptId,
    Long quizId,
    String lastMissedAt,
    String concept
) {}
