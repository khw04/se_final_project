package com.pokemo.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pokemo.auth.config.OAuthProperties;
import com.pokemo.auth.domain.OAuthProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class GoogleOAuthClient implements OAuthClient {

  private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
  private static final String USERINFO_URI = "https://openidconnect.googleapis.com/v1/userinfo";

  private final RestClient restClient;
  private final OAuthProperties.Provider config;

  public GoogleOAuthClient(RestClient oauthRestClient, OAuthProperties properties) {
    this.restClient = oauthRestClient;
    this.config = properties.google();
  }

  @Override
  public OAuthProvider provider() {
    return OAuthProvider.GOOGLE;
  }

  @Override
  public OAuthUserInfo fetchUser(String code, String redirectUri) {
    if (!config.enabled()) {
      throw new AuthException(HttpStatus.SERVICE_UNAVAILABLE, "구글 로그인이 설정되지 않았습니다");
    }

    String accessToken = exchangeAccessToken(code, redirectUri);
    JsonNode profile = fetchProfile(accessToken);

    String sub = text(profile, "sub");
    if (sub == null) {
      throw new AuthException(HttpStatus.UNAUTHORIZED, "구글 사용자 정보를 가져오지 못했습니다");
    }

    return new OAuthUserInfo(
        OAuthProvider.GOOGLE,
        sub,
        text(profile, "email"),
        profile.path("email_verified").asBoolean(false),
        text(profile, "name")
    );
  }

  private String exchangeAccessToken(String code, String redirectUri) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "authorization_code");
    form.add("code", code);
    form.add("client_id", config.clientId());
    form.add("client_secret", config.clientSecret());
    form.add("redirect_uri", redirectUri);

    try {
      JsonNode response = restClient.post()
          .uri(TOKEN_URI)
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(form)
          .retrieve()
          .body(JsonNode.class);

      String accessToken = response == null ? null : text(response, "access_token");
      if (accessToken == null) {
        throw new AuthException(HttpStatus.UNAUTHORIZED, "구글 토큰 교환에 실패했습니다");
      }
      return accessToken;
    } catch (AuthException exception) {
      throw exception;
    } catch (RestClientResponseException exception) {
      throw new AuthException(HttpStatus.UNAUTHORIZED, "구글 인증에 실패했습니다");
    } catch (Exception exception) {
      throw new AuthException(HttpStatus.SERVICE_UNAVAILABLE, "구글 인증 서버 호출에 실패했습니다");
    }
  }

  private JsonNode fetchProfile(String accessToken) {
    try {
      return restClient.get()
          .uri(USERINFO_URI)
          .header("Authorization", "Bearer " + accessToken)
          .retrieve()
          .body(JsonNode.class);
    } catch (RestClientResponseException exception) {
      throw new AuthException(HttpStatus.UNAUTHORIZED, "구글 사용자 정보를 가져오지 못했습니다");
    } catch (Exception exception) {
      throw new AuthException(HttpStatus.SERVICE_UNAVAILABLE, "구글 인증 서버 호출에 실패했습니다");
    }
  }

  private static String text(JsonNode node, String field) {
    JsonNode value = node.get(field);
    return value == null || value.isNull() ? null : value.asText();
  }
}
