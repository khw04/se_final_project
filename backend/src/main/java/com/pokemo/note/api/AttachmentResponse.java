package com.pokemo.note.api;

import com.pokemo.note.domain.Attachment;

public record AttachmentResponse(
    Long id,
    Long noteId,
    String name,
    String mimeType,
    Long size,
    String url
) {
  public static AttachmentResponse from(Attachment a) {
    return new AttachmentResponse(
        a.id(),
        a.noteId(),
        a.fileName(),
        a.mimeType(),
        a.size(),
        "/api/attachments/" + a.id()
    );
  }
}
