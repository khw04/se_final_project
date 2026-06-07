package com.pokemo.ai.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

  @Bean
  RestClient geminiRestClient(AiProperties properties, RestTemplateBuilder builder) {
    return RestClient.builder(builder
        .connectTimeout(Duration.ofSeconds(properties.effectiveTimeoutSeconds()))
        .readTimeout(Duration.ofSeconds(properties.effectiveTimeoutSeconds()))
        .build()).build();
  }
}
