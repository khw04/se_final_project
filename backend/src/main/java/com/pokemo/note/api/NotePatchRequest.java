package com.pokemo.note.api;

public record NotePatchRequest(
    String title,
    String content
) {}
