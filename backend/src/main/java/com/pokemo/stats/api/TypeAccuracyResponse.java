package com.pokemo.stats.api;

public record TypeAccuracyResponse(
    String type,
    int accuracy,
    int attempted,
    int total
) {}
