package com.pokemo.calendar.api;

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
import com.pokemo.calendar.repository.CalendarEventRepository;
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
class CalendarEventControllerTests {

  @Autowired MockMvc mockMvc;
  @Autowired UserAccountRepository userAccountRepository;
  @Autowired AuthTokenRepository authTokenRepository;
  @Autowired CalendarEventRepository calendarEventRepository;
  @Autowired PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    calendarEventRepository.deleteAll();
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
  void createEventReturns201() throws Exception {
    mockMvc.perform(post("/api/events")
            .header("Authorization", "Bearer " + token())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title":"중간고사",
                  "subjectId":1,
                  "startAt":"2026-07-10T09:00:00+09:00",
                  "allDay":false,
                  "type":"exam"
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("중간고사"))
        .andExpect(jsonPath("$.type").value("exam"))
        .andExpect(jsonPath("$.id").isNumber());
  }

  @Test
  void getEventsReturnsCreatedEvent() throws Exception {
    String token = token();
    mockMvc.perform(post("/api/events")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title":"과제마감",
                  "startAt":"2026-07-15T23:59:00+09:00",
                  "allDay":false,
                  "type":"assignment"
                }
                """))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/events").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("과제마감"));
  }

  @Test
  void updateEventReturns200() throws Exception {
    String token = token();
    MvcResult result = mockMvc.perform(post("/api/events")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title":"원래제목",
                  "startAt":"2026-07-20T10:00:00+09:00",
                  "allDay":true,
                  "type":"lecture"
                }
                """))
        .andExpect(status().isCreated())
        .andReturn();

    String id = result.getResponse().getContentAsString()
        .replaceAll(".*\"id\":(\\d+).*", "$1");

    mockMvc.perform(put("/api/events/" + id)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title":"수정된제목",
                  "startAt":"2026-07-20T10:00:00+09:00",
                  "allDay":true,
                  "type":"lecture"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("수정된제목"));
  }

  @Test
  void deleteEventReturns204() throws Exception {
    String token = token();
    MvcResult result = mockMvc.perform(post("/api/events")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title":"삭제할일정",
                  "startAt":"2026-07-25T09:00:00+09:00",
                  "allDay":false,
                  "type":"other"
                }
                """))
        .andExpect(status().isCreated())
        .andReturn();

    String id = result.getResponse().getContentAsString()
        .replaceAll(".*\"id\":(\\d+).*", "$1");

    mockMvc.perform(delete("/api/events/" + id)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
  }

  @Test
  void createRecurringEventReturnsRecurrence() throws Exception {
    mockMvc.perform(post("/api/events")
            .header("Authorization", "Bearer " + token())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title":"주간강의",
                  "startAt":"2026-07-01T09:00:00+09:00",
                  "allDay":false,
                  "type":"lecture",
                  "recurrence":{
                    "freq":"weekly",
                    "interval":1,
                    "byweekday":[1,3],
                    "endMode":"never"
                  }
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.recurrence.freq").value("weekly"))
        .andExpect(jsonPath("$.recurrence.byweekday[0]").value(1));
  }

  @Test
  void getEventsRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/events"))
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
