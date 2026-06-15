package com.pokemo.note.api;

import com.pokemo.note.domain.NoteVersion;

public record NoteVersionResponse(
    Long id,
    Long noteId,
    int versionNumber,
    String title,
    Long subjectId,
    String content,
    String createdAt
) {

  public static NoteVersionResponse from(NoteVersion version) {
    return new NoteVersionResponse(
        version.id(),
        version.noteId(),
        version.versionNumber(),
        version.title(),
        version.subjectId(),
        version.content(),
        version.createdAt().toString());
  }
}
