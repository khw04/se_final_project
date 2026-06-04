package com.pokemo.ai.api;

import java.util.List;

public record AiSummaryResponse(
    Long noteId,
    int sourceCharCount,
    int summaryCharCount,
    List<String> bullets,
    String generatedAt,
    boolean fallback
) {
}
