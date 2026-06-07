package com.pokemo.ai.api;

import java.util.List;

public record RecommendResponse(
    List<PriorityRecommendationResponse> priorities,
    List<WeakConceptResponse> weakConcepts,
    List<UpcomingSubjectResponse> upcomingSubjects,
    boolean fallback
) {
}
