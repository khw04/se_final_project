package com.pokemo.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pokemo.oauth")
public record OAuthProperties(Provider google, Provider kakao) {

  public OAuthProperties {
    if (google == null) {
      google = new Provider(null, null);
    }
    if (kakao == null) {
      kakao = new Provider(null, null);
    }
  }

  public record Provider(String clientId, String clientSecret) {
    public boolean enabled() {
      return clientId != null && !clientId.isBlank();
    }
  }
}
