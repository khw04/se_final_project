package com.pokemo.auth.api;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pokemo.auth.domain.OAuthProvider;
import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.domain.UserRole;
import com.pokemo.auth.repository.AuthTokenRepository;
import com.pokemo.auth.repository.OAuthAccountRepository;
import com.pokemo.auth.repository.UserAccountRepository;
import com.pokemo.auth.service.GoogleOAuthClient;
import com.pokemo.auth.service.OAuthUserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OAuthControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserAccountRepository userAccountRepository;

  @Autowired
  private OAuthAccountRepository oauthAccountRepository;

  @Autowired
  private AuthTokenRepository authTokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockBean
  private GoogleOAuthClient googleOAuthClient;

  @BeforeEach
  void setUp() {
    oauthAccountRepository.deleteAll();
    authTokenRepository.deleteAll();
    userAccountRepository.deleteAll();
    when(googleOAuthClient.provider()).thenReturn(OAuthProvider.GOOGLE);
  }

  @AfterEach
  void tearDown() {
    oauthAccountRepository.deleteAll();
    authTokenRepository.deleteAll();
    userAccountRepository.deleteAll();
  }

  @Test
  void oauthLoginIssuesTokensForValidGoogleCode() throws Exception {
    when(googleOAuthClient.fetchUser(anyString(), anyString())).thenReturn(
        new OAuthUserInfo(OAuthProvider.GOOGLE, "google-sub-1", "social@example.com", true, "Social"));

    mockMvc.perform(post("/api/auth/oauth/google")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"auth-code","redirectUri":"http://localhost:5173"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("social@example.com"))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
        .andExpect(jsonPath("$.refreshToken", not(blankOrNullString())));

    assert userAccountRepository.findByEmail("social@example.com").isPresent();
    assert oauthAccountRepository.count() == 1;
  }

  @Test
  void oauthLoginIssuesTokensForValidKakaoCode() throws Exception {
    UserAccount existing = userAccountRepository.save(new UserAccount(
        "social@example.com",
        passwordEncoder.encode("password123"),
        UserRole.USER
    ));

    when(googleOAuthClient.fetchUser(anyString(), anyString())).thenReturn(
        new OAuthUserInfo(OAuthProvider.GOOGLE, "google-sub-1", "social@example.com", true, "Social"));

    mockMvc.perform(post("/api/auth/oauth/google")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"auth-code","redirectUri":"http://localhost:5173"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(existing.id()));

    assert userAccountRepository.count() == 1;
    assert oauthAccountRepository.count() == 1;
  }

  @Test
  void oauthLoginRejectsInvalidAuthorizationCode() throws Exception {
    mockMvc.perform(post("/api/auth/oauth/naver")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"auth-code","redirectUri":"http://localhost:5173"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }
}
