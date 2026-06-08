package com.pokemo.study.api;

import jakarta.validation.constraints.NotNull;

public record StudySessionRequest(
    @NotNull Long subjectId,
    @NotNull String startedAt,
    @NotNull String endedAt
) {
}
