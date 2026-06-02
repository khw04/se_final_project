package com.pokemo.stats.api;

public record SubjectProgressResponse(
    long subjectId,
    int accuracy,
    int attempted,
    int total
) {}
