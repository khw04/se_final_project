package com.pokemo.ai.api;

public record UpcomingSubjectResponse(
    Long subjectId,
    int dDay,
    Integer accuracy,
    int priorityScore,
    String label
) {
}
