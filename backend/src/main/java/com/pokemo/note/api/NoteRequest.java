package com.pokemo.note.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteRequest(
    @NotBlank @Size(max = 200) String title,
    Long subjectId,
    String content
) {}
