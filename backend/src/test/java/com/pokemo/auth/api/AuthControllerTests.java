package com.pokemo.auth.api;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.domain.UserRole;
import com.pokemo.auth.repository.AuthTokenRepository;
import com.pokemo.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserAccountRepository userAccountRepository;

  @Autowired
  private AuthTokenRepository authTokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    authTokenRepository.deleteAll();
    userAccountRepository.deleteAll();
  }

  @Test
  void registersUserWithBcryptPassword() throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"Student@Example.com","password":"password123"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("student@example.com"))
        .andExpect(jsonPath("$.role").value("USER"));

    UserAccount user = userAccountRepository.findByEmail("student@example.com").orElseThrow();
    assert passwordEncoder.matches("password123", user.passwordHash());
  }

  @Test
  void loginReturnsTokens() throws Exception {
    UserAccount verifiedUser = new UserAccount(
        "student@example.com",
        passwordEncoder.encode("password123"),
        UserRole.USER
    );
    verifiedUser.markEmailVerified();
    userAccountRepository.save(verifiedUser);

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com","password":"password123"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
        .andExpect(jsonPath("$.refreshToken", not(blankOrNullString())));
  }

  @Test
  void refreshRotatesStoredRefreshToken() throws Exception {
    UserAccount verifiedUser = new UserAccount(
        "student@example.com",
        passwordEncoder.encode("password123"),
        UserRole.USER
    );
    verifiedUser.markEmailVerified();
    userAccountRepository.save(verifiedUser);

    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com","password":"password123"}
                """))
        .andExpect(status().isOk())
        .andReturn();

    String refreshToken = loginResult.getResponse().getContentAsString()
        .replaceAll(".*\"refreshToken\":\"([^\"]+)\".*", "$1");

    mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
        .andExpect(jsonPath("$.refreshToken", not(refreshToken)));

    mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void meReturnsCurrentUserForAccessToken() throws Exception {
    UserAccount verifiedUser = new UserAccount(
        "student@example.com",
        passwordEncoder.encode("password123"),
        UserRole.USER
    );
    verifiedUser.markEmailVerified();
    userAccountRepository.save(verifiedUser);

    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com","password":"password123"}
                """))
        .andExpect(status().isOk())
        .andReturn();

    String accessToken = loginResult.getResponse().getContentAsString()
        .replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");

    mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("student@example.com"));
  }

  @Test
  void loginReturnsNotFoundForUnknownEmail() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"missing@example.com","password":"password123"}
                """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void loginReturnsUnauthorizedForWrongPassword() throws Exception {
    UserAccount verifiedUser = new UserAccount(
        "student@example.com",
        passwordEncoder.encode("password123"),
        UserRole.USER
    );
    verifiedUser.markEmailVerified();
    userAccountRepository.save(verifiedUser);

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com","password":"wrong-password"}
                """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401));
  }

  @Test
  void logoutRevokesRefreshToken() throws Exception {
    UserAccount verifiedUser = new UserAccount(
        "student@example.com",
        passwordEncoder.encode("password123"),
        UserRole.USER
    );
    verifiedUser.markEmailVerified();
    userAccountRepository.save(verifiedUser);

    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com","password":"password123"}
                """))
        .andExpect(status().isOk())
        .andReturn();

    String responseBody = loginResult.getResponse().getContentAsString();
    String accessToken = responseBody.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
    String refreshToken = responseBody.replaceAll(".*\"refreshToken\":\"([^\"]+)\".*", "$1");

    mockMvc.perform(post("/api/auth/logout")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
        .andExpect(status().isNoContent());

    mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginReturnsForbiddenForUnverifiedEmail() throws Exception {
    userAccountRepository.save(new UserAccount(
        "student@example.com",
        passwordEncoder.encode("password123"),
        UserRole.USER
    ));

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com","password":"password123"}
                """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void logoutRequiresAuthentication() throws Exception {
    mockMvc.perform(post("/api/auth/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"some-token\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void protectedEndpointRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/protected-placeholder"))
        .andExpect(status().isForbidden());
  }

  @Test
  void responsesIncludeSecurityHeaders() throws Exception {
    mockMvc.perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(header().exists("Content-Security-Policy"))
        .andExpect(header().exists("Referrer-Policy"))
        .andExpect(header().exists("Permissions-Policy"))
        .andExpect(header().exists("X-Frame-Options"));
  }
}
