package com.pokemo.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OAuthProperties.class)
public class OAuthClientConfig {

  @Bean
  RestClient oauthRestClient(RestTemplateBuilder builder) {
    return RestClient.builder(builder
        .connectTimeout(Duration.ofSeconds(5))
        .readTimeout(Duration.ofSeconds(8))
        .build()).build();
  }
}
