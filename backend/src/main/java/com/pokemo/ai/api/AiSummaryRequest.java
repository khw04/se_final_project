package com.pokemo.ai.api;

import jakarta.validation.constraints.NotNull;

public record AiSummaryRequest(@NotNull Long noteId) {
}
