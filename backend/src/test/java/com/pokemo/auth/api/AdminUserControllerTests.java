package com.pokemo.auth.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.domain.UserRole;
import com.pokemo.auth.repository.AuthTokenRepository;
import com.pokemo.auth.repository.UserAccountRepository;
import com.pokemo.auth.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserControllerTests {

  @Autowired MockMvc mockMvc;
  @Autowired UserAccountRepository userAccountRepository;
  @Autowired AuthTokenRepository authTokenRepository;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired JwtTokenService jwtTokenService;

  @BeforeEach
  void setUp() {
    authTokenRepository.deleteAll();
    userAccountRepository.deleteAll();
  }

  @Test
  void adminCanPromoteUser() throws Exception {
    UserAccount target = userAccountRepository.save(user("target@example.com", UserRole.USER));

    mockMvc.perform(post("/api/admin/users/" + target.id() + "/promote")
            .header("Authorization", "Bearer " + tokenFor(UserRole.ADMIN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(target.id()))
        .andExpect(jsonPath("$.role").value("ADMIN"));
  }

  @Test
  void userCannotPromoteUser() throws Exception {
    UserAccount target = userAccountRepository.save(user("target@example.com", UserRole.USER));

    mockMvc.perform(post("/api/admin/users/" + target.id() + "/promote")
            .header("Authorization", "Bearer " + tokenFor(UserRole.USER)))
        .andExpect(status().isForbidden());
  }

  private String tokenFor(UserRole role) {
    UserAccount admin = userAccountRepository.save(user(role.name().toLowerCase() + "@example.com", role));
    return jwtTokenService.createAccessToken(admin);
  }

  private UserAccount user(String email, UserRole role) {
    UserAccount user = new UserAccount(email, passwordEncoder.encode("password123"), role);
    user.markEmailVerified();
    return user;
  }
}
