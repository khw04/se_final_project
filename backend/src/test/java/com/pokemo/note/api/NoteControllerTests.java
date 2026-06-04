package com.pokemo.note.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.domain.UserRole;
import com.pokemo.auth.repository.AuthTokenRepository;
import com.pokemo.auth.repository.UserAccountRepository;
import com.pokemo.note.repository.NoteRepository;
import com.pokemo.note.repository.TagRepository;
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
class NoteControllerTests {

  @Autowired MockMvc mockMvc;
  @Autowired UserAccountRepository userAccountRepository;
  @Autowired AuthTokenRepository authTokenRepository;
  @Autowired NoteRepository noteRepository;
  @Autowired TagRepository tagRepository;
  @Autowired PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    noteRepository.deleteAll();
    tagRepository.deleteAll();
    authTokenRepository.deleteAll();
    userAccountRepository.deleteAll();
    userAccountRepository.save(new UserAccount(
        "test@pokemo.dev",
        passwordEncoder.encode("pass1234"),
        UserRole.USER
    ));
  }

  @Test
  void createNoteReturns201() throws Exception {
    mockMvc.perform(post("/api/notes")
            .header("Authorization", "Bearer " + token())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"극한의 정의","subjectId":1,"content":"## 내용\\nε-δ 정의"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("극한의 정의"))
        .andExpect(jsonPath("$.id").isNumber());
  }

  @Test
  void getNotesReturnsCreatedNote() throws Exception {
    String token = token();
    mockMvc.perform(post("/api/notes")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"자료구조 노트","content":"해시 충돌 정리"}
                """))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/notes").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("자료구조 노트"));
  }

  @Test
  void patchNoteUpdatesContent() throws Exception {
    String token = token();
    MvcResult result = mockMvc.perform(post("/api/notes")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"원래제목","content":"원래내용"}
                """))
        .andExpect(status().isCreated())
        .andReturn();

    String id = result.getResponse().getContentAsString()
        .replaceAll(".*\"id\":(\\d+).*", "$1");

    mockMvc.perform(patch("/api/notes/" + id)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"content":"수정된내용"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정된내용"));
  }

  @Test
  void deleteNoteReturns204() throws Exception {
    String token = token();
    MvcResult result = mockMvc.perform(post("/api/notes")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"삭제할노트","content":""}
                """))
        .andExpect(status().isCreated())
        .andReturn();

    String id = result.getResponse().getContentAsString()
        .replaceAll(".*\"id\":(\\d+).*", "$1");

    mockMvc.perform(delete("/api/notes/" + id)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
  }

  @Test
  void searchNotesByQuery() throws Exception {
    String token = token();
    mockMvc.perform(post("/api/notes")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"극한의 정의","content":"ε-δ 정의로 설명"}
                """))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/notes")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"해시 충돌","content":"체이닝 기법"}
                """))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/notes?q=극한").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("극한의 정의"));
  }

  @Test
  void createTagAndAddToNote() throws Exception {
    String token = token();

    MvcResult tagResult = mockMvc.perform(post("/api/tags")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"핵심"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("핵심"))
        .andReturn();

    String tagId = tagResult.getResponse().getContentAsString()
        .replaceAll(".*\"id\":(\\d+).*", "$1");

    MvcResult noteResult = mockMvc.perform(post("/api/notes")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"태그테스트","content":""}
                """))
        .andExpect(status().isCreated())
        .andReturn();

    String noteId = noteResult.getResponse().getContentAsString()
        .replaceAll(".*\"id\":(\\d+).*", "$1");

    mockMvc.perform(post("/api/notes/" + noteId + "/tags/" + tagId)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tagIds[0]").value(Integer.parseInt(tagId)));
  }

  @Test
  void createTagConflictOnDuplicate() throws Exception {
    String token = token();
    mockMvc.perform(post("/api/tags")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"중복태그"}
                """))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/tags")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"중복태그"}
                """))
        .andExpect(status().isConflict());
  }

  @Test
  void getNotesRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/notes"))
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
