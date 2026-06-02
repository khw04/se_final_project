package com.pokemo.stats.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pokemo.auth.domain.UserAccount;
import com.pokemo.auth.domain.UserRole;
import com.pokemo.auth.repository.AuthTokenRepository;
import com.pokemo.auth.repository.UserAccountRepository;
import com.pokemo.quiz.repository.QuestionRepository;
import com.pokemo.quiz.repository.QuizAttemptRepository;
import com.pokemo.quiz.repository.QuizRepository;
import com.pokemo.quiz.repository.WrongAnswerNoteRepository;
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
class StatsControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired AuthTokenRepository authTokenRepository;
    @Autowired QuizRepository quizRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired QuizAttemptRepository attemptRepository;
    @Autowired WrongAnswerNoteRepository wrongAnswerNoteRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        wrongAnswerNoteRepository.deleteAll();
        attemptRepository.deleteAll();
        quizRepository.deleteAll();
        questionRepository.deleteAll();
        authTokenRepository.deleteAll();
        userAccountRepository.deleteAll();

        userAccountRepository.save(new UserAccount(
                "stats@test.com", passwordEncoder.encode("pass1234"), UserRole.USER));

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"stats@test.com","password":"pass1234"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        accessToken = login.getResponse().getContentAsString()
                .replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void 데이터_없을때_통계_빈배열_반환() throws Exception {
        mockMvc.perform(get("/api/stats/trend")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/stats/progress")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/stats/weekly")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void 퀴즈_풀이후_통계_조회() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/quizzes")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "통계용 퀴즈",
                                  "subjectId": 2,
                                  "questions": [
                                    {
                                      "type": "OX",
                                      "text": "true?",
                                      "correctBool": true,
                                      "explanation": "맞다",
                                      "difficulty": "EASY",
                                      "subjectId": 2
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String quizId = createResult.getResponse().getContentAsString()
                .replaceAll(".*\"id\":([0-9]+).*", "$1");
        String questionId = createResult.getResponse().getContentAsString()
                .replaceAll(".*\"questionIds\":\\[([0-9]+)\\].*", "$1");

        mockMvc.perform(post("/api/quizzes/" + quizId + "/attempts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"answers":[{"questionId":%s,"userAnswer":"true","timeSpentSec":10}]}
                                """.formatted(questionId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/stats/progress")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].subjectId").value(2))
                .andExpect(jsonPath("$[0].accuracy").value(100));

        mockMvc.perform(get("/api/stats/trend")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].accuracy").value(100.0));
    }
}
