package com.pokemo.study.api;

import java.util.List;

public record TodayStudySummaryResponse(
    int totalSeconds,
    List<TodayStudySubjectResponse> subjects
) {
}
