package com.pokemo.notice.api;

import com.pokemo.notice.domain.Notice;
import java.time.OffsetDateTime;

public record NoticeResponse(
    Long id,
    String title,
    String body,
    String tag,
    String author,
    boolean pinned,
    long viewCount,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

  public static NoticeResponse from(Notice notice) {
    return new NoticeResponse(
        notice.id(),
        notice.title(),
        notice.body(),
        notice.tag().name(),
        notice.author(),
        notice.pinned(),
        notice.viewCount(),
        notice.createdAt(),
        notice.updatedAt()
    );
  }
}
