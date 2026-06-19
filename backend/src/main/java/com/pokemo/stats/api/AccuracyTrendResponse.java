package com.pokemo.stats.api;

public record AccuracyTrendResponse(
    String attemptedAt,
    int accuracy
) {}
