package com.pokemo.stats.api;

public record WeeklyStudyResponse(
    int weekday,
    String weekdayLabel,
    int studyMinutes,
    int studySeconds
) {}
