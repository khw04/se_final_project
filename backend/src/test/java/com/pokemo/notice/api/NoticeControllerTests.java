package com.pokemo.notice.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.domain.UserRole;
import com.pokemo.auth.repository.AuthTokenRepository;
import com.pokemo.auth.repository.UserAccountRepository;
import com.pokemo.auth.service.JwtTokenService;
import com.pokemo.notice.domain.Notice;
import com.pokemo.notice.domain.NoticeTag;
import com.pokemo.notice.repository.NoticeRepository;
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
class NoticeControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private NoticeRepository noticeRepository;

  @Autowired
  private UserAccountRepository userAccountRepository;

  @Autowired
  private AuthTokenRepository authTokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenService jwtTokenService;

  @BeforeEach
  void setUp() {
    noticeRepository.deleteAll();
    authTokenRepository.deleteAll();
    userAccountRepository.deleteAll();
  }

  private String tokenFor(UserRole role) {
    UserAccount user = userAccountRepository.save(new UserAccount(
        role.name().toLowerCase() + "@example.com",
        passwordEncoder.encode("password123"),
        role
    ));
    return jwtTokenService.createAccessToken(user);
  }

  @Test
  void listReturnsNoticesPinnedFirstWithoutAuthentication() throws Exception {
    noticeRepository.save(new Notice("일반 공지", "본문", NoticeTag.공지, false, "운영팀"));
    noticeRepository.save(new Notice("고정 공지", "본문", NoticeTag.점검, true, "운영팀"));

    mockMvc.perform(get("/api/notices"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].title").value("고정 공지"))
        .andExpect(jsonPath("$[0].pinned").value(true));
  }

  @Test
  void detailIncrementsViewCount() throws Exception {
    Notice notice = noticeRepository.save(new Notice("조회 테스트", "본문", NoticeTag.공지, false, "운영팀"));

    mockMvc.perform(get("/api/notices/" + notice.id()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.viewCount").value(1));

    mockMvc.perform(get("/api/notices/" + notice.id()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.viewCount").value(2));
  }

  @Test
  void adminCanCreateNotice() throws Exception {
    mockMvc.perform(post("/api/notices")
            .header("Authorization", "Bearer " + tokenFor(UserRole.ADMIN))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"점검 안내","body":"## 서버 점검","tag":"점검","pinned":true}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("점검 안내"))
        .andExpect(jsonPath("$.tag").value("점검"))
        .andExpect(jsonPath("$.pinned").value(true))
        .andExpect(jsonPath("$.author").value("admin@example.com"))
        .andExpect(jsonPath("$.viewCount").value(0));
  }

  @Test
  void userCannotCreateNotice() throws Exception {
    mockMvc.perform(post("/api/notices")
            .header("Authorization", "Bearer " + tokenFor(UserRole.USER))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"점검 안내","body":"본문","tag":"공지","pinned":false}
                """))
        .andExpect(status().isForbidden());
  }

  @Test
  void unauthenticatedCannotCreateNotice() throws Exception {
    mockMvc.perform(post("/api/notices")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"점검 안내","body":"본문","tag":"공지","pinned":false}
                """))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCanUpdateNotice() throws Exception {
    Notice notice = noticeRepository.save(new Notice("수정 전", "본문", NoticeTag.공지, false, "운영팀"));

    mockMvc.perform(put("/api/notices/" + notice.id())
            .header("Authorization", "Bearer " + tokenFor(UserRole.ADMIN))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"수정 후","body":"새 본문","tag":"런칭","pinned":true}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("수정 후"))
        .andExpect(jsonPath("$.tag").value("런칭"))
        .andExpect(jsonPath("$.pinned").value(true));
  }

  @Test
  void adminCanDeleteNotice() throws Exception {
    Notice notice = noticeRepository.save(new Notice("삭제 대상", "본문", NoticeTag.공지, false, "운영팀"));

    mockMvc.perform(delete("/api/notices/" + notice.id())
            .header("Authorization", "Bearer " + tokenFor(UserRole.ADMIN)))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/notices/" + notice.id()))
        .andExpect(status().isNotFound());
  }

  @Test
  void createRejectsBlankTitle() throws Exception {
    mockMvc.perform(post("/api/notices")
            .header("Authorization", "Bearer " + tokenFor(UserRole.ADMIN))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"  ","body":"본문","tag":"공지","pinned":false}
                """))
        .andExpect(status().isBadRequest());
  }
}
