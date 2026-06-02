package com.pokemo.auth.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pokemo.auth.domain.EmailVerification;
import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.domain.UserRole;
import com.pokemo.auth.repository.EmailVerificationRepository;
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
class EmailVerificationControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private EmailVerificationRepository emailVerificationRepository;

  @Autowired
  private UserAccountRepository userAccountRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    emailVerificationRepository.deleteAll();
    userAccountRepository.deleteAll();
  }

  @Test
  void verifyRequestStoresCodeWithoutAuthentication() throws Exception {
    mockMvc.perform(post("/api/auth/email/verify-request")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"Student@Example.com"}
                """))
        .andExpect(status().isAccepted());

    assertThat(emailVerificationRepository.findFirstByEmailAndConsumedFalseOrderByIdDesc("student@example.com"))
        .isPresent();
  }

  @Test
  void verifySucceedsAndMarksUserVerified() throws Exception {
    userAccountRepository.save(new UserAccount(
        "student@example.com",
        passwordEncoder.encode("password123"),
        UserRole.USER
    ));

    mockMvc.perform(post("/api/auth/email/verify-request")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com"}
                """))
        .andExpect(status().isAccepted());

    EmailVerification verification = emailVerificationRepository
        .findFirstByEmailAndConsumedFalseOrderByIdDesc("student@example.com")
        .orElseThrow();

    mockMvc.perform(post("/api/auth/email/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"student@example.com\",\"code\":\"" + verification.code() + "\"}"))
        .andExpect(status().isNoContent());

    assertThat(userAccountRepository.findByEmail("student@example.com").orElseThrow().emailVerified())
        .isTrue();
  }

  @Test
  void verifyFailsWithWrongCode() throws Exception {
    mockMvc.perform(post("/api/auth/email/verify-request")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com"}
                """))
        .andExpect(status().isAccepted());

    mockMvc.perform(post("/api/auth/email/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"student@example.com","code":"000000"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }
}
