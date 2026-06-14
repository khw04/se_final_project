package com.pokemo.note.api;

import com.pokemo.note.domain.Tag;

public record TagResponse(Long id, String name) {

  public static TagResponse from(Tag tag) {
    return new TagResponse(tag.id(), tag.name());
  }
}
