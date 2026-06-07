package com.pokemo.ai.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record QuizGenerateRequest(
    @NotNull Long noteId,
    @Min(1) @Max(10) Integer count
) {
  public int effectiveCount() {
    return count == null ? 5 : count;
  }
}
