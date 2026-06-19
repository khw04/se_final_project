package com.pokemo.quiz.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemo.common.ApiException;
import com.pokemo.quiz.api.AnswerCheckRequest;
import com.pokemo.quiz.api.AnswerCheckResponse;
import com.pokemo.quiz.api.AnswerRequest;
import com.pokemo.quiz.api.AttemptRequest;
import com.pokemo.quiz.api.AttemptResponse;
import com.pokemo.quiz.api.QuestionRequest;
import com.pokemo.quiz.api.QuestionResponse;
import com.pokemo.quiz.api.QuizRequest;
import com.pokemo.quiz.api.QuizResponse;
import com.pokemo.quiz.api.WrongAnswerResponse;
import com.pokemo.quiz.domain.AttemptAnswer;
import com.pokemo.quiz.domain.GeneratedBy;
import com.pokemo.quiz.domain.Question;
import com.pokemo.quiz.domain.QuestionType;
import com.pokemo.quiz.domain.Quiz;
import com.pokemo.quiz.domain.QuizAttempt;
import com.pokemo.quiz.domain.QuizQuestion;
import com.pokemo.quiz.domain.WrongAnswerNote;
import com.pokemo.quiz.repository.QuestionRepository;
import com.pokemo.quiz.repository.QuizAttemptRepository;
import com.pokemo.quiz.repository.QuizRepository;
import com.pokemo.quiz.repository.WrongAnswerNoteRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final WrongAnswerNoteRepository wrongAnswerNoteRepository;
    private final ObjectMapper objectMapper;

    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository,
            QuizAttemptRepository attemptRepository,
            WrongAnswerNoteRepository wrongAnswerNoteRepository, ObjectMapper objectMapper) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.wrongAnswerNoteRepository = wrongAnswerNoteRepository;
        this.objectMapper = objectMapper;
    }

    public QuizResponse createQuiz(long userId, QuizRequest request) {
        Quiz quiz = createQuizEntity(userId, request, GeneratedBy.MANUAL, null);
        return toQuizResponse(quizRepository.save(quiz), false);
    }

    public QuizResponse createAiQuiz(long userId, long noteId, QuizRequest request) {
        Quiz quiz = createQuizEntity(userId, request, GeneratedBy.AI_GEMINI, noteId);
        return toQuizResponse(quizRepository.save(quiz), false);
    }

    private Quiz createQuizEntity(long userId, QuizRequest request, GeneratedBy generatedBy, Long noteId) {
        Quiz quiz = new Quiz(request.title(), request.subjectId(), userId, generatedBy);
        if (noteId != null) {
            quiz.setGeneratedFromNoteId(noteId);
        }

        List<Question> savedQuestions = new ArrayList<>();
        for (QuestionRequest qr : request.questions()) {
            Question q = new Question(qr.type(), qr.text(), qr.explanation(),
                    qr.difficulty(), qr.subjectId(), userId);
            applyAnswer(q, qr);
            if (qr.conceptTags() != null) {
                q.setConceptTagsJson(toJson(qr.conceptTags()));
            }
            savedQuestions.add(questionRepository.save(q));
        }

        for (int i = 0; i < savedQuestions.size(); i++) {
            quiz.addQuestion(savedQuestions.get(i), i);
        }

        return quiz;
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> listQuizzes(long userId) {
        return quizRepository.findByCreatedByOrderByCreatedAtDesc(userId)
                .stream().map(quiz -> toQuizResponse(quiz, false)).toList();
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuiz(long userId, long quizId) {
        Quiz quiz = findOwnedQuiz(userId, quizId);
        return toQuizResponse(quiz, false);
    }

    @Transactional(readOnly = true)
    public AnswerCheckResponse checkAnswer(long userId, long quizId, long questionId, AnswerCheckRequest request) {
        Quiz quiz = findOwnedQuiz(userId, quizId);
        Question question = quiz.quizQuestions().stream()
                .map(QuizQuestion::question)
                .filter(q -> q.id().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "문제를 찾을 수 없습니다."));
        boolean correct = checkAnswer(question, request.userAnswer());
        return new AnswerCheckResponse(
                question.id(),
                correct,
                question.correctIndex(),
                question.correctText(),
                question.correctBool(),
                question.explanation());
    }

    public AttemptResponse submitAttempt(long userId, long quizId, AttemptRequest request) {
        Quiz quiz = findOwnedQuiz(userId, quizId);

        Map<Long, Question> questionMap = quiz.quizQuestions().stream()
                .map(QuizQuestion::question)
                .collect(Collectors.toMap(Question::id, q -> q));

        List<AttemptAnswer> attemptAnswers = request.answers().stream()
                .map(ar -> {
                    Question q = questionMap.get(ar.questionId());
                    if (q == null) return null;
                    return new AttemptAnswer(ar.questionId(), ar.userAnswer(),
                            checkAnswer(q, ar.userAnswer()), ar.timeSpentSec());
                })
                .filter(a -> a != null)
                .toList();

        QuizAttempt attempt = new QuizAttempt(userId, quizId, quiz.quizQuestions().size());
        attempt.complete(attemptAnswers);
        QuizAttempt saved = attemptRepository.save(attempt);

        saved.answers().stream()
                .filter(a -> !a.correct())
                .forEach(a -> {
                    Question q = questionMap.get(a.questionId());
                    if (q == null) return;
                    String concept = fromJson(q.conceptTagsJson()).stream().findFirst().orElse("");
                    wrongAnswerNoteRepository.findByUserIdAndQuestionId(userId, a.questionId())
                            .ifPresentOrElse(
                                    note -> note.incrementMiss(saved.id()),
                                    () -> wrongAnswerNoteRepository.save(
                                            new WrongAnswerNote(userId, a.questionId(), saved.id(), concept)));
                });

        return toAttemptResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WrongAnswerResponse> getWrongAnswers(long userId, String type) {
        List<WrongAnswerNote> notes = wrongAnswerNoteRepository.findByUserIdOrderByMissCountDesc(userId);

        Map<Long, Question> questionMap = questionRepository
                .findAllById(notes.stream().map(WrongAnswerNote::questionId).toList())
                .stream().collect(Collectors.toMap(Question::id, q -> q));

        List<Long> attemptIds = notes.stream()
                .map(WrongAnswerNote::lastAttemptId)
                .filter(id -> id != null)
                .distinct().toList();
        Map<Long, Long> attemptToQuizId = attemptRepository.findAllById(attemptIds)
                .stream().collect(Collectors.toMap(QuizAttempt::id, QuizAttempt::quizId));

        return notes.stream()
                .filter(note -> {
                    if (type == null || type.isBlank() || "all".equalsIgnoreCase(type)) return true;
                    Question q = questionMap.get(note.questionId());
                    return q != null && q.type().name().equalsIgnoreCase(type);
                })
                .map(note -> toWrongAnswerResponse(note, questionMap.get(note.questionId()),
                        note.lastAttemptId() != null ? attemptToQuizId.get(note.lastAttemptId()) : null))
                .filter(r -> r.question() != null)
                .toList();
    }

    // 오답노트에서 다시 풀어 맞힌 문제를 목록에서 제거한다.
    public void resolveWrongAnswer(long userId, long questionId) {
        wrongAnswerNoteRepository.findByUserIdAndQuestionId(userId, questionId)
                .ifPresent(wrongAnswerNoteRepository::delete);
    }

    public QuizResponse retryWeakTypes(long userId) {
        List<WrongAnswerNote> repeated = wrongAnswerNoteRepository
                .findByUserIdOrderByMissCountDesc(userId)
                .stream().filter(n -> n.missCount() >= 2).toList();

        if (repeated.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "재시험할 취약 문제가 없습니다");
        }

        List<Question> questions = questionRepository
                .findAllById(repeated.stream().map(WrongAnswerNote::questionId).toList());

        Long subjectId = questions.isEmpty() ? 1L : questions.get(0).subjectId();
        Quiz quiz = new Quiz("취약 유형 재시험", subjectId, userId, GeneratedBy.MANUAL);
        for (int i = 0; i < questions.size(); i++) {
            quiz.addQuestion(questions.get(i), i);
        }
        return toQuizResponse(quizRepository.save(quiz), false);
    }

    private Quiz findOwnedQuiz(long userId, long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "퀴즈를 찾을 수 없습니다"));
        if (!quiz.createdBy().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다");
        }
        return quiz;
    }

    private void applyAnswer(Question q, QuestionRequest qr) {
        switch (qr.type()) {
            case MCQ -> {
                if (qr.choices() != null && qr.correctIndex() != null) {
                    q.setMcqAnswer(toJson(qr.choices()), qr.correctIndex());
                }
            }
            case SHORT -> {
                if (qr.correctText() != null) q.setShortAnswer(qr.correctText());
            }
            case OX -> {
                if (qr.correctBool() != null) q.setOxAnswer(qr.correctBool());
            }
        }
    }

    private boolean checkAnswer(Question q, String userAnswer) {
        if (userAnswer == null) return false;
        return switch (q.type()) {
            case MCQ -> {
                try {
                    yield q.correctIndex() != null && q.correctIndex() == Integer.parseInt(userAnswer);
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
            case OX -> q.correctBool() != null
                    && q.correctBool().toString().equalsIgnoreCase(userAnswer.trim());
            case SHORT -> q.correctText() != null
                    && q.correctText().trim().equalsIgnoreCase(userAnswer.trim());
        };
    }

    private QuizResponse toQuizResponse(Quiz quiz, boolean includeAnswers) {
        List<QuestionResponse> questions = quiz.quizQuestions().stream()
                .map(qq -> toQuestionResponse(qq.question(), includeAnswers))
                .toList();
        List<Long> questionIds = questions.stream().map(QuestionResponse::id).toList();

        return new QuizResponse(
                quiz.id(), quiz.title(), quiz.subjectId(),
                toGeneratedByString(quiz.generatedBy()),
                quiz.generatedFromNoteId(),
                questionIds, questions,
                quiz.createdAt().toString());
    }

    private QuestionResponse toQuestionResponse(Question q, boolean includeAnswers) {
        return new QuestionResponse(
                q.id(), q.type().name().toLowerCase(), q.text(),
                fromJson(q.choicesJson()),
                includeAnswers ? q.correctIndex() : null,
                includeAnswers ? q.correctText() : null,
                includeAnswers ? q.correctBool() : null,
                includeAnswers ? q.explanation() : null,
                q.difficulty().name().toLowerCase(),
                q.subjectId(), fromJson(q.conceptTagsJson()), q.createdAt().toString());
    }

    private AttemptResponse toAttemptResponse(QuizAttempt a) {
        return new AttemptResponse(
                a.id(), a.quizId(), a.totalQuestions(), a.correctCount(), a.accuracy(),
                a.startedAt().toString(),
                a.completedAt() != null ? a.completedAt().toString() : null);
    }

    private WrongAnswerResponse toWrongAnswerResponse(WrongAnswerNote note, Question q, Long quizId) {
        return new WrongAnswerResponse(
                note.questionId(),
                q != null ? toQuestionResponse(q, true) : null,
                note.missCount(), note.lastAttemptId(), quizId,
                note.lastMissedAt().toString(), note.concept());
    }

    private String toGeneratedByString(GeneratedBy g) {
        return switch (g) {
            case MANUAL -> "manual";
            case AI_GPT -> "ai-gpt";
            case AI_GEMINI -> "ai-gemini";
        };
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list != null ? list : List.of());
        } catch (Exception e) {
            return "[]";
        }
    }
}
