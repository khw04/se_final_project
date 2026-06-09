package com.pokemo.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pokemo.ai.config.AiProperties;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GeminiAiClient implements AiClient {

  private final RestClient restClient;
  private final AiProperties properties;

  public GeminiAiClient(RestClient geminiRestClient, AiProperties properties) {
    this.restClient = geminiRestClient;
    this.properties = properties;
  }

  @Override
  public String generateText(String prompt) {
    if (!properties.enabled()) {
      throw new AiClientException("Gemini API 키가 설정되지 않았습니다.");
    }

    URI uri = UriComponentsBuilder
        .fromUriString("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent")
        .queryParam("key", properties.apiKey())
        .buildAndExpand(properties.model())
        .toUri();

    Map<String, Object> body = Map.of(
        "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
        "generationConfig", Map.of(
            "temperature", 1.2,
            "topP", 0.95,
            "topK", 40,
            "responseMimeType", "application/json"
        )
    );

    try {
      JsonNode response = restClient.post()
          .uri(uri)
          .body(body)
          .retrieve()
          .body(JsonNode.class);

      JsonNode text = response == null ? null : response.at("/candidates/0/content/parts/0/text");
      if (text == null || text.isMissingNode() || text.asText().isBlank()) {
        throw new AiClientException("Gemini 응답이 비어 있습니다.");
      }
      return text.asText();
    } catch (AiClientException exception) {
      throw exception;
    } catch (RestClientResponseException exception) {
      throw new AiClientException("Gemini 호출에 실패했습니다. status="
          + exception.getStatusCode().value() + ", body=" + exception.getResponseBodyAsString(), exception);
    } catch (ResourceAccessException exception) {
      throw new AiClientException("Gemini 호출 시간이 초과되었습니다.", exception);
    } catch (Exception exception) {
      throw new AiClientException("Gemini 호출에 실패했습니다.", exception);
    }
  }
}
