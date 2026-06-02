package com.pokemo.stats.api;

public record AccuracyTrendResponse(
    String attemptedAt,
    double accuracy
) {}
