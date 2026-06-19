package com.pokemo.ai.api;

public record UpcomingSubjectResponse(
    Long subjectId,
    String subjectName,
    int dDay,
    Integer accuracy,
    int priorityScore,
    String label
) {
}
