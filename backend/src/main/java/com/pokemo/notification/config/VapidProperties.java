package com.pokemo.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pokemo.push.vapid")
public record VapidProperties(String publicKey, String privateKey, String subject) {

  public VapidProperties {
    if (subject == null || subject.isBlank()) {
      subject = "mailto:admin@pokemo.dev";
    }
  }

  public boolean configured() {
    return publicKey != null && !publicKey.isBlank()
        && privateKey != null && !privateKey.isBlank();
  }
}
