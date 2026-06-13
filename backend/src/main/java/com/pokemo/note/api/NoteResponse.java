package com.pokemo.note.api;

import com.pokemo.note.domain.Note;
import java.util.List;

public record NoteResponse(
    Long id,
    String title,
    Long subjectId,
    String content,
    List<Long> attachmentIds,
    String preview,
    String updatedAt,
    String createdAt
) {

  private static final int PREVIEW_LENGTH = 100;

  public static NoteResponse from(Note note, List<Long> attachmentIds) {
    String preview = note.content() == null || note.content().isBlank()
        ? ""
        : note.content().replaceAll("#+ |\\*\\*|~~|`", "").strip();
    if (preview.length() > PREVIEW_LENGTH) {
      preview = preview.substring(0, PREVIEW_LENGTH) + "...";
    }
    return new NoteResponse(
        note.id(),
        note.title(),
        note.subjectId(),
        note.content(),
        List.copyOf(attachmentIds),
        preview,
        note.updatedAt().toString(),
        note.createdAt().toString()
    );
  }
}
