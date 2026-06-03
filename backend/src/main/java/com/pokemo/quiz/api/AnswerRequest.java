package com.pokemo.quiz.api;

import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
    @NotNull Long questionId,
    String userAnswer,
    int timeSpentSec
) {}
