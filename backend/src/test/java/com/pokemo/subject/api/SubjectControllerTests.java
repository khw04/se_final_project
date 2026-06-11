package com.pokemo.subject.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.domain.UserRole;
import com.pokemo.auth.repository.AuthTokenRepository;
import com.pokemo.auth.repository.UserAccountRepository;
import com.pokemo.subject.repository.SubjectRepository;
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
class SubjectControllerTests {

  @Autowired MockMvc mockMvc;
  @Autowired UserAccountRepository userAccountRepository;
  @Autowired AuthTokenRepository authTokenRepository;
  @Autowired SubjectRepository subjectRepository;
  @Autowired PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
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
  void getSubjectsReturnsSeededList() throws Exception {
    mockMvc.perform(get("/api/subjects").header("Authorization", "Bearer " + token()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void createSubjectReturns201() throws Exception {
    mockMvc.perform(post("/api/subjects")
            .header("Authorization", "Bearer " + token())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"테스트과목","color":"#123456"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("테스트과목"))
        .andExpect(jsonPath("$.color").value("#123456"));
  }

  @Test
  void createSubjectConflictOnDuplicateName() throws Exception {
    String token = token();
    mockMvc.perform(post("/api/subjects")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"중복과목","color":"#aabbcc"}
                """))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/subjects")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"중복과목","color":"#112233"}
                """))
        .andExpect(status().isConflict());
  }

  @Test
  void deleteSubjectReturns204() throws Exception {
    String token = token();
    MvcResult result = mockMvc.perform(post("/api/subjects")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"삭제할과목","color":"#ff0000"}
                """))
        .andExpect(status().isCreated())
        .andReturn();

    String body = result.getResponse().getContentAsString();
    String id = body.replaceAll(".*\"id\":(\\d+).*", "$1");

    mockMvc.perform(delete("/api/subjects/" + id)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
  }

  @Test
  void getSubjectsRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/subjects"))
        .andExpect(status().isForbidden());
  }

  private String token() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"test@pokemo.dev","password":"pass1234"}
                """))
        .andReturn();
    return result.getResponse().getContentAsString()
        .replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
  }
}
