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
    createUser("test@pokemo.dev");
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
        .andExpect(jsonPath("$.tagIds.length()").value(0))
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
  void patchNoteUpdatesSubject() throws Exception {
    String token = token();
    MvcResult result = mockMvc.perform(post("/api/notes")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"과목변경","subjectId":1,"content":""}
                """))
        .andExpect(status().isCreated())
        .andReturn();

    String id = result.getResponse().getContentAsString()
        .replaceAll(".*\"id\":(\\d+).*", "$1");

    mockMvc.perform(patch("/api/notes/" + id)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"subjectId":2}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.subjectId").value(2));
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
  void pagedNotesReturnPageMetadata() throws Exception {
    String token = token();
    createNote(token, "첫 번째 노트", "내용1");
    createNote(token, "두 번째 노트", "내용2");

    mockMvc.perform(get("/api/notes/paged?page=0&size=1").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(1))
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  void searchQueryIsBoundAsParameter() throws Exception {
    String token = token();
    createNote(token, "정상 노트", "내용");

    mockMvc.perform(get("/api/notes?q=' OR 1=1 --").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void patchCreatesVersionAndRestoreRevertsContent() throws Exception {
    String token = token();
    String noteId = createNote(token, "버전 노트", "처음 내용");

    mockMvc.perform(patch("/api/notes/" + noteId)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"수정 노트","content":"수정 내용"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("수정 노트"));

    MvcResult versions = mockMvc.perform(get("/api/notes/" + noteId + "/versions")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("버전 노트"))
        .andReturn();

    String versionId = versions.getResponse().getContentAsString()
        .replaceAll(".*\"id\":(\\d+).*", "$1");

    mockMvc.perform(post("/api/notes/" + noteId + "/versions/" + versionId + "/restore")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("버전 노트"))
        .andExpect(jsonPath("$.content").value("처음 내용"));
  }

  @Test
  void createAndListTags() throws Exception {
    String token = token();

    mockMvc.perform(post("/api/tags")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"미적분"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.name").value("미적분"));

    mockMvc.perform(get("/api/tags").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("미적분"));
  }

  @Test
  void createDuplicateTagReturnsConflict() throws Exception {
    String token = token();

    createTag(token, "운영체제");

    mockMvc.perform(post("/api/tags")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"운영체제"}
                """))
        .andExpect(status().isConflict());
  }

  @Test
  void deleteTagRemovesItFromTaggedNotes() throws Exception {
    String token = token();
    String noteId = createNote(token, "삭제 대상 노트", "태그가 붙어 있습니다");
    String tagId = createTag(token, "삭제태그");

    mockMvc.perform(post("/api/notes/" + noteId + "/tags/" + tagId)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tagIds.length()").value(1));

    mockMvc.perform(delete("/api/tags/" + tagId)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/notes/" + noteId)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tagIds.length()").value(0));
  }

  @Test
  void addAndRemoveTagOnNote() throws Exception {
    String token = token();
    String noteId = createNote(token, "태그 노트", "태그 테스트");
    String tagId = createTag(token, "중요");

    mockMvc.perform(post("/api/notes/" + noteId + "/tags/" + tagId)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tagIds.length()").value(1))
        .andExpect(jsonPath("$.tagIds[0]").value(Integer.parseInt(tagId)));

    mockMvc.perform(delete("/api/notes/" + noteId + "/tags/" + tagId)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tagIds.length()").value(0));
  }

  @Test
  void cannotAddAnotherUsersTagToOwnNote() throws Exception {
    String ownerToken = token();
    String ownerTagId = createTag(ownerToken, "소유자태그");
    createUser("other@pokemo.dev");
    String otherToken = tokenFor("other@pokemo.dev");
    String otherNoteId = createNote(otherToken, "다른 사용자 노트", "내용");

    mockMvc.perform(post("/api/notes/" + otherNoteId + "/tags/" + ownerTagId)
            .header("Authorization", "Bearer " + otherToken))
        .andExpect(status().isNotFound());
  }

  @Test
  void cannotDeleteAnotherUsersTag() throws Exception {
    String ownerToken = token();
    String ownerTagId = createTag(ownerToken, "소유자태그");
    createUser("other@pokemo.dev");
    String otherToken = tokenFor("other@pokemo.dev");

    mockMvc.perform(delete("/api/tags/" + ownerTagId)
            .header("Authorization", "Bearer " + otherToken))
        .andExpect(status().isNotFound());
  }

  @Test
  void filterNotesByTagIds() throws Exception {
    String token = token();
    String targetNoteId = createNote(token, "필터 대상", "그래프 이론");
    createNote(token, "필터 제외", "정렬 알고리즘");
    String tagId = createTag(token, "시험범위");

    mockMvc.perform(post("/api/notes/" + targetNoteId + "/tags/" + tagId)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/notes?tagIds=" + tagId).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(Integer.parseInt(targetNoteId)))
        .andExpect(jsonPath("$[0].tagIds[0]").value(Integer.parseInt(tagId)));
  }

  @Test
  void searchNotesByTagName() throws Exception {
    String token = token();
    String targetNoteId = createNote(token, "태그 검색 대상", "본문에는 태그명이 없음");
    createNote(token, "태그 검색 제외", "본문에도 태그명이 없음");
    String tagId = createTag(token, "중간고사");

    mockMvc.perform(post("/api/notes/" + targetNoteId + "/tags/" + tagId)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/notes?q=중간고사").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(Integer.parseInt(targetNoteId)));
  }

  @Test
  void getNotesRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/notes"))
        .andExpect(status().isForbidden());
  }

  private String token() throws Exception {
    return tokenFor("test@pokemo.dev");
  }

  private String tokenFor(String email) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"pass1234\"}"))
        .andReturn();
    return result.getResponse().getContentAsString()
        .replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
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

  private String createNote(String token, String title, String content) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/notes")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"" + title + "\",\"content\":\"" + content + "\"}"))
        .andExpect(status().isCreated())
        .andReturn();
    return idFrom(result);
  }

  private String createTag(String token, String name) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/tags")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"" + name + "\"}"))
        .andExpect(status().isCreated())
        .andReturn();
    return idFrom(result);
  }

  private String idFrom(MvcResult result) throws Exception {
    return result.getResponse().getContentAsString()
        .replaceAll(".*\"id\":(\\d+).*", "$1");
  }
}
