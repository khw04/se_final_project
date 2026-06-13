package com.pokemo.quiz.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class QuizControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
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

        UserAccount user = new UserAccount(
                "quiz@test.com", passwordEncoder.encode("pass1234"), UserRole.USER);
        user.markEmailVerified();
        userAccountRepository.save(user);

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"quiz@test.com","password":"pass1234"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(login.getResponse().getContentAsString());
        accessToken = loginJson.get("accessToken").asText();
    }

    @Test
    void 퀴즈_생성후_목록_조회() throws Exception {
        mockMvc.perform(post("/api/quizzes")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "테스트 퀴즈",
                                  "subjectId": 1,
                                  "questions": [
                                    {
                                      "type": "MCQ",
                                      "text": "다음 중 옳은 것은?",
                                      "choices": ["①", "②", "③", "④"],
                                      "correctIndex": 1,
                                      "explanation": "②가 정답",
                                      "difficulty": "EASY",
                                      "subjectId": 1
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("테스트 퀴즈"))
                .andExpect(jsonPath("$.questions", hasSize(1)));

        mockMvc.perform(get("/api/quizzes")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void 풀이_제출시_정답률_계산() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/quizzes")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "OX 퀴즈",
                                  "subjectId": 1,
                                  "questions": [
                                    {
                                      "type": "OX",
                                      "text": "1+1=2 인가?",
                                      "correctBool": true,
                                      "explanation": "맞다",
                                      "difficulty": "EASY",
                                      "subjectId": 1
                                    },
                                    {
                                      "type": "OX",
                                      "text": "2+2=5 인가?",
                                      "correctBool": false,
                                      "explanation": "틀리다",
                                      "difficulty": "EASY",
                                      "subjectId": 1
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode quiz = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long quizId = quiz.get("id").asLong();
        long questionId1 = quiz.get("questionIds").get(0).asLong();

        mockMvc.perform(post("/api/quizzes/" + quizId + "/attempts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "answers": [
                                    {"questionId": %d, "userAnswer": "true", "timeSpentSec": 5}
                                  ]
                                }
                                """.formatted(questionId1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correctCount").value(1))
                .andExpect(jsonPath("$.totalQuestions").value(2));
    }

    @Test
    void 오답_제출시_오답노트_생성() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/quizzes")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "단답 퀴즈",
                                  "subjectId": 1,
                                  "questions": [
                                    {
                                      "type": "SHORT",
                                      "text": "1+1=?",
                                      "correctText": "2",
                                      "explanation": "2다",
                                      "difficulty": "EASY",
                                      "subjectId": 1,
                                      "conceptTags": ["덧셈"]
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode quiz = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long quizId = quiz.get("id").asLong();
        long questionId = quiz.get("questionIds").get(0).asLong();

        mockMvc.perform(post("/api/quizzes/" + quizId + "/attempts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"answers":[{"questionId":%d,"userAnswer":"3","timeSpentSec":3}]}
                                """.formatted(questionId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correctCount").value(0));

        mockMvc.perform(get("/api/wrong-answers")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].missCount").value(1))
                .andExpect(jsonPath("$[0].concept").value("덧셈"));
    }

    @Test
    void 인증없이_퀴즈_조회_거부() throws Exception {
        mockMvc.perform(get("/api/quizzes"))
                .andExpect(status().is4xxClientError());
    }
}
