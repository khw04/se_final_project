package com.pokemo.ai.api;

public record PriorityRecommendationResponse(
    int rank,
    Long subjectId,
    String reason,
    Integer dDay,
    Integer accuracy,
    String tone
) {
}
