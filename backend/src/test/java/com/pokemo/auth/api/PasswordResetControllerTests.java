package com.pokemo.auth.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pokemo.auth.domain.PasswordResetToken;
import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.domain.UserRole;
import com.pokemo.auth.repository.PasswordResetTokenRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordResetControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Autowired
  private UserAccountRepository userAccountRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    passwordResetTokenRepository.deleteAll();
    userAccountRepository.deleteAll();
  }

  private void saveUser() {
    userAccountRepository.save(new UserAccount(
        "student@example.com",
        passwordEncoder.encode("oldpassword"),
        UserRole.USER
    ));
  }

  @Test
  void resetRequestCreatesTokenForExistingUser() throws Exception {
    saveUser();

    mockMvc.perform(post("/api/auth/password/reset-request")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com"}
                """))
        .andExpect(status().isAccepted());

    assertThat(passwordResetTokenRepository.findAll()).hasSize(1);
  }

  @Test
  void resetRequestDoesNotLeakUnknownEmail() throws Exception {
    mockMvc.perform(post("/api/auth/password/reset-request")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"missing@example.com"}
                """))
        .andExpect(status().isAccepted());

    assertThat(passwordResetTokenRepository.findAll()).isEmpty();
  }

  @Test
  void resetChangesPasswordAndConsumesToken() throws Exception {
    saveUser();

    mockMvc.perform(post("/api/auth/password/reset-request")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com"}
                """))
        .andExpect(status().isAccepted());

    PasswordResetToken token = passwordResetTokenRepository.findAll().get(0);

    mockMvc.perform(post("/api/auth/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token.token() + "\",\"newPassword\":\"newpassword123\"}"))
        .andExpect(status().isNoContent());

    UserAccount user = userAccountRepository.findByEmail("student@example.com").orElseThrow();
    assertThat(passwordEncoder.matches("newpassword123", user.passwordHash())).isTrue();

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com","password":"newpassword123"}
                """))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com","password":"oldpassword"}
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void resetRejectsInvalidToken() throws Exception {
    mockMvc.perform(post("/api/auth/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"token":"non-existent-token","newPassword":"newpassword123"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void validateAcceptsUsableTokenAndRejectsConsumed() throws Exception {
    saveUser();

    mockMvc.perform(post("/api/auth/password/reset-request")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com"}
                """))
        .andExpect(status().isAccepted());

    PasswordResetToken token = passwordResetTokenRepository.findAll().get(0);

    mockMvc.perform(post("/api/auth/password/reset-validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token.token() + "\"}"))
        .andExpect(status().isNoContent());

    mockMvc.perform(post("/api/auth/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token.token() + "\",\"newPassword\":\"newpassword123\"}"))
        .andExpect(status().isNoContent());

    mockMvc.perform(post("/api/auth/password/reset-validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token.token() + "\"}"))
        .andExpect(status().isBadRequest());
  }
}
