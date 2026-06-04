package com.pokemo.ai.api;

public record UpcomingSubjectResponse(
    Long subjectId,
    int dDay,
    int accuracy,
    int priorityScore,
    String label
) {
}
