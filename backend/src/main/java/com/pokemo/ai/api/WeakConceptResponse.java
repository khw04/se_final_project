package com.pokemo.ai.api;

public record WeakConceptResponse(
    String concept,
    Long subjectId,
    int missCount,
    int totalAttempts,
    java.util.List<String> relatedKeywords
) {
}
