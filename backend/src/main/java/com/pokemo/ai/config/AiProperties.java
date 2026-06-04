package com.pokemo.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pokemo.ai")
public record AiProperties(
    String apiKey,
    String model,
    int timeoutSeconds
) {
  public boolean enabled() {
    return apiKey != null && !apiKey.isBlank();
  }

  public int effectiveTimeoutSeconds() {
    return timeoutSeconds > 0 ? timeoutSeconds : 8;
  }
}
