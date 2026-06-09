package com.pokemo.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemo.ai.api.AiSummaryResponse;
import com.pokemo.ai.api.PriorityRecommendationResponse;
import com.pokemo.ai.api.RecommendResponse;
import com.pokemo.ai.api.UpcomingSubjectResponse;
import com.pokemo.ai.api.WeakConceptResponse;
import com.pokemo.calendar.domain.CalendarEvent;
import com.pokemo.calendar.repository.CalendarEventRepository;
import com.pokemo.common.ApiException;
import com.pokemo.note.domain.Note;
import com.pokemo.note.repository.NoteRepository;
import com.pokemo.quiz.api.QuestionRequest;
import com.pokemo.quiz.api.QuizRequest;
import com.pokemo.quiz.api.QuizResponse;
import com.pokemo.quiz.domain.Difficulty;
import com.pokemo.quiz.domain.Question;
import com.pokemo.quiz.domain.Quiz;
import com.pokemo.quiz.domain.QuestionType;
import com.pokemo.quiz.domain.WrongAnswerNote;
import com.pokemo.quiz.repository.QuestionRepository;
import com.pokemo.quiz.repository.QuizAttemptRepository;
import com.pokemo.quiz.repository.QuizRepository;
import com.pokemo.quiz.repository.WrongAnswerNoteRepository;
import com.pokemo.quiz.service.QuizService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiService {

  private final AiClient aiClient;
  private final ObjectMapper objectMapper;
  private final NoteRepository noteRepository;
  private final QuizService quizService;
  private final QuizRepository quizRepository;
  private final QuestionRepository questionRepository;
  private final WrongAnswerNoteRepository wrongAnswerNoteRepository;
  private final CalendarEventRepository calendarEventRepository;
  private final QuizAttemptRepository quizAttemptRepository;

  public AiService(
      AiClient aiClient,
      ObjectMapper objectMapper,
      NoteRepository noteRepository,
      QuizService quizService,
      QuizRepository quizRepository,
      QuestionRepository questionRepository,
      WrongAnswerNoteRepository wrongAnswerNoteRepository,
      CalendarEventRepository calendarEventRepository,
      QuizAttemptRepository quizAttemptRepository
  ) {
    this.aiClient = aiClient;
    this.objectMapper = objectMapper;
    this.noteRepository = noteRepository;
    this.quizService = quizService;
    this.quizRepository = quizRepository;
    this.questionRepository = questionRepository;
    this.wrongAnswerNoteRepository = wrongAnswerNoteRepository;
    this.calendarEventRepository = calendarEventRepository;
    this.quizAttemptRepository = quizAttemptRepository;
  }

  @Transactional(readOnly = true)
  public AiSummaryResponse summarizeNote(long userId, long noteId) {
    Note note = findOwnedNote(userId, noteId);
    String content = requireContent(note);
    String prompt = """
        다음 학습 노트를 한국어로 요약해라.
        반드시 JSON만 반환해라. 형식: {"bullets":["핵심 1","핵심 2","핵심 3"]}
        각 bullet은 80자 이내로 3~5개 작성한다.

        제목: %s
        내용:
        %s
        """.formatted(note.title(), content);

    try {
      JsonNode root = parseJson(aiClient.generateText(prompt));
      List<String> bullets = strings(root.get("bullets"));
      if (bullets.isEmpty()) {
        throw new AiClientException("요약 bullet이 비어 있습니다.");
      }
      int summaryLength = bullets.stream().mapToInt(String::length).sum();
      return new AiSummaryResponse(note.id(), content.length(), summaryLength, bullets,
          OffsetDateTime.now().toString(), false);
    } catch (AiClientException exception) {
      throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }
  }

  public QuizResponse generateQuiz(long userId, long noteId, int count) {
    Note note = findOwnedNote(userId, noteId);
    String content = requireContent(note);
    String recentQuestions = recentQuestionTexts(userId, noteId);
    String variationSeed = UUID.randomUUID().toString();
    String prompt = """
        다음 학습 노트로 퀴즈 %d개를 생성해라.
        반드시 JSON만 반환해라. 형식:
        {"title":"노트 기반 퀴즈","questions":[{"type":"MCQ|SHORT|OX","text":"문제","choices":["A","B","C","D"],"correctIndex":0,"correctText":"정답","correctBool":true,"explanation":"해설","difficulty":"EASY|MEDIUM|HARD","conceptTags":["개념"]}]}
        MCQ는 choices 4개와 correctIndex를 포함하고, SHORT는 correctText, OX는 correctBool을 포함한다.
        이전에 낸 문제와 같은 문장, 같은 수치, 같은 보기 조합을 반복하지 마라.
        문제 유형은 가능한 한 MCQ, SHORT, OX가 섞이게 하고, 개념 태그도 서로 다르게 배분해라.
        같은 개념을 묻더라도 상황, 조건, 표현, 오답 선택지를 바꿔 새 문제처럼 만들어라.
        아래 변형 seed를 참고해 매번 다른 관점과 예시로 출제해라.
        변형 seed: %s
        최근 생성된 문제 목록:
        %s

        제목: %s
        내용:
        %s
        """.formatted(count, variationSeed, recentQuestions, note.title(), content);

    try {
      JsonNode root = parseJson(aiClient.generateText(prompt));
      QuizRequest request = toQuizRequest(root, note);
      return quizService.createAiQuiz(userId, noteId, request);
    } catch (AiClientException exception) {
      throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }
  }

  @Transactional(readOnly = true)
  public RecommendResponse recommend(long userId) {
    List<WeakConceptResponse> weakConcepts = weakConcepts(userId);
    List<UpcomingSubjectResponse> upcomingSubjects = upcomingSubjects(userId);
    List<PriorityRecommendationResponse> priorities = priorities(userId, weakConcepts, upcomingSubjects);
    return new RecommendResponse(priorities, weakConcepts, upcomingSubjects, true);
  }

  private Note findOwnedNote(long userId, long noteId) {
    Note note = noteRepository.findById(noteId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "노트를 찾을 수 없습니다."));
    if (!note.userId().equals(userId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }
    return note;
  }

  private String requireContent(Note note) {
    if (note.content() == null || note.content().isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "AI 처리할 노트 내용이 비어 있습니다.");
    }
    return note.content().trim();
  }

  private JsonNode parseJson(String raw) {
    String text = raw.trim();
    if (text.startsWith("```")) {
      text = text.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
    }
    try {
      return objectMapper.readTree(text);
    } catch (Exception exception) {
      throw new AiClientException("AI 응답 JSON 파싱에 실패했습니다.", exception);
    }
  }

  private QuizRequest toQuizRequest(JsonNode root, Note note) {
    String title = text(root.get("title"), note.title() + " AI 퀴즈");
    List<QuestionRequest> questions = new ArrayList<>();
    JsonNode questionNodes = root.get("questions");
    if (questionNodes != null && questionNodes.isArray()) {
      for (JsonNode node : questionNodes) {
        questions.add(toQuestionRequest(node, note.subjectId()));
      }
    }
    if (questions.isEmpty()) {
      throw new AiClientException("AI가 생성한 문제가 없습니다.");
    }
    return new QuizRequest(title, note.subjectId() == null ? 1L : note.subjectId(), questions);
  }

  private String recentQuestionTexts(long userId, long noteId) {
    List<String> texts = quizRepository.findByCreatedByOrderByCreatedAtDesc(userId).stream()
        .filter(quiz -> quiz.generatedFromNoteId() != null && quiz.generatedFromNoteId().equals(noteId))
        .limit(5)
        .flatMap((Quiz quiz) -> quiz.quizQuestions().stream())
        .map(quizQuestion -> "- " + quizQuestion.question().text())
        .limit(20)
        .toList();
    return texts.isEmpty() ? "- 없음" : String.join("\n", texts);
  }

  private QuestionRequest toQuestionRequest(JsonNode node, Long fallbackSubjectId) {
    QuestionType type = enumValue(QuestionType.class, text(node.get("type"), "MCQ"), QuestionType.MCQ);
    Difficulty difficulty = enumValue(Difficulty.class, text(node.get("difficulty"), "MEDIUM"), Difficulty.MEDIUM);
    List<String> choices = strings(node.get("choices"));
    Integer correctIndex = node.hasNonNull("correctIndex") ? node.get("correctIndex").asInt() : null;
    String correctText = node.hasNonNull("correctText") ? node.get("correctText").asText() : null;
    Boolean correctBool = node.hasNonNull("correctBool") ? node.get("correctBool").asBoolean() : null;
    return new QuestionRequest(
        type,
        text(node.get("text"), "AI 생성 문제"),
        choices,
        correctIndex,
        correctText,
        correctBool,
        text(node.get("explanation"), "노트 내용을 다시 확인하세요."),
        difficulty,
        node.hasNonNull("subjectId") ? node.get("subjectId").asLong() : (fallbackSubjectId == null ? 1L : fallbackSubjectId),
        strings(node.get("conceptTags"))
    );
  }

  private List<WeakConceptResponse> weakConcepts(long userId) {
    List<WrongAnswerNote> notes = wrongAnswerNoteRepository.findByUserIdOrderByMissCountDesc(userId).stream()
        .limit(5)
        .toList();
    Map<Long, Long> subjectByQuestionId = questionRepository.findAllById(
            notes.stream().map(WrongAnswerNote::questionId).toList()
        ).stream()
        .collect(Collectors.toMap(Question::id, Question::subjectId));
    return notes.stream()
        .map(note -> new WeakConceptResponse(
            note.concept() == null || note.concept().isBlank() ? "취약 유형" : note.concept(),
            subjectByQuestionId.get(note.questionId()),
            note.missCount(),
            Math.max(note.missCount(), 1),
            List.of("오답 복습", "개념 정리")))
        .toList();
  }

  private List<UpcomingSubjectResponse> upcomingSubjects(long userId) {
    LocalDate today = LocalDate.now();
    return calendarEventRepository.findByUserIdOrderByStartAtAsc(userId).stream()
        .filter(event -> !event.startAt().toLocalDate().isBefore(today))
        .limit(5)
        .map(event -> {
          int dDay = (int) ChronoUnit.DAYS.between(today, event.startAt().toLocalDate());
          int priorityScore = Math.max(20, Math.min(100, 100 - dDay * 10));
          return new UpcomingSubjectResponse(event.subjectId(), dDay, 60, priorityScore, event.title());
        })
        .toList();
  }

  private List<PriorityRecommendationResponse> priorities(
      long userId,
      List<WeakConceptResponse> weakConcepts,
      List<UpcomingSubjectResponse> upcomingSubjects
  ) {
    List<PriorityRecommendationResponse> result = new ArrayList<>();
    upcomingSubjects.stream()
        .sorted(Comparator.comparingInt(UpcomingSubjectResponse::dDay))
        .limit(2)
        .forEach(item -> result.add(new PriorityRecommendationResponse(result.size() + 1,
            item.subjectId(), "시험·과제 일정이 임박했습니다: " + item.label(), item.dDay(), item.accuracy(), item.dDay() <= 3 ? "urgent" : "warning")));
    weakConcepts.stream().limit(2).forEach(item -> result.add(new PriorityRecommendationResponse(result.size() + 1,
        item.subjectId(), "오답이 누적된 개념을 복습하세요: " + item.concept(), null, 50, "warning")));
    if (result.isEmpty()) {
      int solved = quizAttemptRepository.findByUserIdOrderByStartedAtDesc(userId).size();
      result.add(new PriorityRecommendationResponse(1, null,
          solved > 0 ? "최근 퀴즈 정답률을 유지하기 위해 새 문제를 풀어보세요." : "노트를 작성하고 첫 퀴즈를 생성해보세요.", null, 0, "normal"));
    }
    return result;
  }

  private List<String> strings(JsonNode node) {
    if (node == null || !node.isArray()) return List.of();
    List<String> values = new ArrayList<>();
    node.forEach(item -> {
      if (item != null && !item.asText().isBlank()) values.add(item.asText());
    });
    return values;
  }

  private String text(JsonNode node, String fallback) {
    return node == null || node.asText().isBlank() ? fallback : node.asText();
  }

  private <T extends Enum<T>> T enumValue(Class<T> type, String value, T fallback) {
    try {
      return Enum.valueOf(type, value.toUpperCase());
    } catch (Exception exception) {
      return fallback;
    }
  }
}
