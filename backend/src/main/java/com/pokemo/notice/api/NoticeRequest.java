package com.pokemo.notice.api;

import com.pokemo.notice.domain.NoticeTag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoticeRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank String body,
    @NotNull NoticeTag tag,
    boolean pinned
) {
}
