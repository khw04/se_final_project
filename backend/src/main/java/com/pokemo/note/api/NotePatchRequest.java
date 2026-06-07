package com.pokemo.note.api;

public record NotePatchRequest(
    String title,
    Long subjectId,
    String content
) {}
