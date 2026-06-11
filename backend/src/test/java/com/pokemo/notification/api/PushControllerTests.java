package com.pokemo.notification.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.domain.UserRole;
import com.pokemo.auth.repository.AuthTokenRepository;
import com.pokemo.auth.repository.UserAccountRepository;
import com.pokemo.notification.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PushControllerTests {

  @Autowired MockMvc mockMvc;
  @Autowired UserAccountRepository userAccountRepository;
  @Autowired AuthTokenRepository authTokenRepository;
  @Autowired PushSubscriptionRepository pushSubscriptionRepository;
  @Autowired PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    pushSubscriptionRepository.deleteAll();
    authTokenRepository.deleteAll();
    userAccountRepository.deleteAll();
    UserAccount user = new UserAccount(
        "test@pokemo.dev",
        passwordEncoder.encode("pass1234"),
        UserRole.USER
    );
    user.markEmailVerified();
    userAccountRepository.save(user);
  }

  @Test
  void publicKeyReturnsBase64UrlEncodedKey() throws Exception {
    mockMvc.perform(get("/api/push/public-key")
            .header("Authorization", "Bearer " + token()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicKey").isString())
        .andExpect(jsonPath("$.publicKey").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString())));
  }

  @Test
  void subscribeStoresSubscription() throws Exception {
    mockMvc.perform(post("/api/push/subscribe")
            .header("Authorization", "Bearer " + token())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "endpoint": "https://push.example.com/abc123",
                  "keys": {"p256dh": "p256dh-key", "auth": "auth-key"}
                }
                """))
        .andExpect(status().isNoContent());

    assertThat(pushSubscriptionRepository.findAll()).hasSize(1);
    assertThat(pushSubscriptionRepository.findAll().get(0).endpoint())
        .isEqualTo("https://push.example.com/abc123");
  }

  @Test
  void unsubscribeRemovesSubscription() throws Exception {
    String accessToken = token();

    mockMvc.perform(post("/api/push/subscribe")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "endpoint": "https://push.example.com/to-remove",
                  "keys": {"p256dh": "p256dh-key", "auth": "auth-key"}
                }
                """))
        .andExpect(status().isNoContent());

    mockMvc.perform(delete("/api/push/subscribe")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"endpoint": "https://push.example.com/to-remove"}
                """))
        .andExpect(status().isNoContent());

    assertThat(pushSubscriptionRepository.findAll()).isEmpty();
  }

  @Test
  void subscribeRequiresAuthentication() throws Exception {
    mockMvc.perform(post("/api/push/subscribe")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "endpoint": "https://push.example.com/abc123",
                  "keys": {"p256dh": "p256dh-key", "auth": "auth-key"}
                }
                """))
        .andExpect(status().isForbidden());
  }

  private String token() throws Exception {
    var result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"test@pokemo.dev","password":"pass1234"}
                """))
        .andReturn();
    return result.getResponse().getContentAsString()
        .replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
  }
}
