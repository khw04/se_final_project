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
    subjectRepository.deleteAll();
    userAccountRepository.deleteAll();
    createUser("test@pokemo.dev");
    createUser("other@pokemo.dev");
  }

  private void createUser(String email) {
    UserAccount user = new UserAccount(
        email,
        passwordEncoder.encode("pass1234"),
        UserRole.USER
    );
    user.markEmailVerified();
    userAccountRepository.save(user);
  }

  @Test
  void getSubjectsReturnsArray() throws Exception {
    mockMvc.perform(get("/api/subjects").header("Authorization", "Bearer " + token("test@pokemo.dev")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void createSubjectReturns201() throws Exception {
    mockMvc.perform(post("/api/subjects")
            .header("Authorization", "Bearer " + token("test@pokemo.dev"))
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
    String token = token("test@pokemo.dev");
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
    String token = token("test@pokemo.dev");
    String id = createSubject(token, "삭제할과목", "#ff0000");

    mockMvc.perform(delete("/api/subjects/" + id)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
  }

  @Test
  void getSubjectsRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/subjects"))
        .andExpect(status().isForbidden());
  }

  @Test
  void subjectsAreIsolatedPerUser() throws Exception {
    String owner = token("test@pokemo.dev");
    createSubject(owner, "내과목", "#abcdef");

    // 같은 이름이라도 다른 사용자는 충돌 없이 자기 과목을 만들 수 있다.
    String other = token("other@pokemo.dev");
    mockMvc.perform(post("/api/subjects")
            .header("Authorization", "Bearer " + other)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"내과목","color":"#000000"}
                """))
        .andExpect(status().isCreated());

    // 다른 사용자의 과목 목록에는 본인 과목만 보인다.
    mockMvc.perform(get("/api/subjects").header("Authorization", "Bearer " + other))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].color").value("#000000"));
  }

  @Test
  void cannotDeleteOtherUsersSubject() throws Exception {
    String owner = token("test@pokemo.dev");
    String id = createSubject(owner, "보호과목", "#123123");

    String other = token("other@pokemo.dev");
    mockMvc.perform(delete("/api/subjects/" + id)
            .header("Authorization", "Bearer " + other))
        .andExpect(status().isForbidden());
  }

  private String createSubject(String token, String name, String color) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/subjects")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"" + name + "\",\"color\":\"" + color + "\"}"))
        .andExpect(status().isCreated())
        .andReturn();
    String body = result.getResponse().getContentAsString();
    return body.replaceAll(".*\"id\":(\\d+).*", "$1");
  }

  private String token(String email) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"pass1234\"}"))
        .andReturn();
    return result.getResponse().getContentAsString()
        .replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
  }
}
