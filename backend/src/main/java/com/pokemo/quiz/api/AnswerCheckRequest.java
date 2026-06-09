package com.pokemo.quiz.api;

import jakarta.validation.constraints.NotNull;

public record AnswerCheckRequest(
    @NotNull String userAnswer
) {}
