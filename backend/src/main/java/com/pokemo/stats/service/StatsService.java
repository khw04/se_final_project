package com.pokemo.stats.service;

import com.pokemo.quiz.domain.AttemptAnswer;
import com.pokemo.quiz.domain.Question;
import com.pokemo.quiz.domain.QuestionType;
import com.pokemo.quiz.domain.Quiz;
import com.pokemo.quiz.domain.QuizAttempt;
import com.pokemo.quiz.repository.QuestionRepository;
import com.pokemo.quiz.repository.QuizAttemptRepository;
import com.pokemo.quiz.repository.QuizRepository;
import com.pokemo.stats.api.AccuracyTrendResponse;
import com.pokemo.stats.api.SubjectProgressResponse;
import com.pokemo.stats.api.TypeAccuracyResponse;
import com.pokemo.stats.api.WeeklyStudyResponse;
import com.pokemo.study.repository.StudySessionRepository;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private static final String[] WEEKDAY_LABELS = {"일", "월", "화", "수", "목", "금", "토"};

    private final QuizAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final StudySessionRepository studySessionRepository;

    public StatsService(
            QuizAttemptRepository attemptRepository,
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            StudySessionRepository studySessionRepository
    ) {
        this.attemptRepository = attemptRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.studySessionRepository = studySessionRepository;
    }

    @Cacheable(value = "accuracyTrend", key = "#userId")
    public List<AccuracyTrendResponse> getAccuracyTrend(long userId) {
        return attemptRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .filter(a -> a.completedAt() != null)
                .map(a -> new AccuracyTrendResponse(a.startedAt().toString(), a.accuracy()))
                .toList();
    }

    @Cacheable(value = "subjectProgress", key = "#userId")
    public List<SubjectProgressResponse> getSubjectProgress(long userId) {
        List<QuizAttempt> attempts = attemptRepository.findByUserIdOrderByStartedAtDesc(userId);
        if (attempts.isEmpty()) return List.of();

        Map<Long, Quiz> quizMap = quizRepository
                .findAllById(attempts.stream().map(QuizAttempt::quizId).distinct().toList())
                .stream().collect(Collectors.toMap(Quiz::id, q -> q));

        record SubjectStat(int attempted, int correct) {}

        Map<Long, SubjectStat> statBySubject = attempts.stream()
                .filter(a -> quizMap.containsKey(a.quizId()))
                .collect(Collectors.groupingBy(
                        a -> quizMap.get(a.quizId()).subjectId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> new SubjectStat(
                                        list.stream().mapToInt(QuizAttempt::totalQuestions).sum(),
                                        list.stream().mapToInt(QuizAttempt::correctCount).sum()
                                )
                        )
                ));

        Map<Long, Integer> totalBySubject = questionRepository.findByCreatedBy(userId).stream()
                .collect(Collectors.groupingBy(
                        q -> q.subjectId(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        return statBySubject.entrySet().stream()
                .map(e -> {
                    SubjectStat s = e.getValue();
                    int accuracy = s.attempted() > 0 ? (int) Math.round((double) s.correct() / s.attempted() * 100) : 0;
                    int total = Math.max(s.attempted(), totalBySubject.getOrDefault(e.getKey(), s.attempted()));
                    return new SubjectProgressResponse(e.getKey(), accuracy, s.attempted(), total);
                })
                .toList();
    }

    @Cacheable(value = "typeAccuracy", key = "#userId")
    public List<TypeAccuracyResponse> getTypeAccuracy(long userId) {
        List<QuizAttempt> attempts = attemptRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .filter(a -> a.completedAt() != null)
                .toList();
        if (attempts.isEmpty()) return List.of();

        List<AttemptAnswer> allAnswers = attempts.stream()
                .flatMap(a -> a.answers().stream())
                .toList();

        Map<Long, QuestionType> questionTypeById = questionRepository
                .findAllById(allAnswers.stream().map(AttemptAnswer::questionId).distinct().toList())
                .stream().collect(Collectors.toMap(Question::id, Question::type));

        record TypeStat(int attempted, int correct) {}

        Map<QuestionType, TypeStat> statByType = allAnswers.stream()
                .filter(a -> questionTypeById.containsKey(a.questionId()))
                .collect(Collectors.groupingBy(
                        a -> questionTypeById.get(a.questionId()),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> new TypeStat(
                                        list.size(),
                                        (int) list.stream().filter(AttemptAnswer::correct).count()
                                )
                        )
                ));

        Map<QuestionType, Integer> totalByType = questionRepository.findByCreatedBy(userId).stream()
                .collect(Collectors.groupingBy(
                        Question::type,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        return Arrays.stream(QuestionType.values())
                .map(type -> {
                    TypeStat s = statByType.getOrDefault(type, new TypeStat(0, 0));
                    int accuracy = s.attempted() > 0 ? (int) Math.round((double) s.correct() / s.attempted() * 100) : 0;
                    int total = Math.max(s.attempted(), totalByType.getOrDefault(type, s.attempted()));
                    return new TypeAccuracyResponse(type.name(), accuracy, s.attempted(), total);
                })
                .toList();
    }

    @Cacheable(value = "weeklyStudy", key = "#userId")
    public List<WeeklyStudyResponse> getWeeklyStudy(long userId) {
        Map<Integer, Long> secondsByWeekday = new HashMap<>(attemptRepository
                .findByUserIdOrderByStartedAtDesc(userId).stream()
                .filter(a -> a.completedAt() != null)
                .collect(Collectors.groupingBy(
                        a -> a.startedAt().getDayOfWeek().getValue() % 7,
                        Collectors.summingLong(a -> Duration.between(a.startedAt(), a.completedAt()).toSeconds())
                )));

        studySessionRepository.findByUserIdOrderByStartedAtDesc(userId).forEach(session -> {
            int weekday = session.startedAt().getDayOfWeek().getValue() % 7;
            secondsByWeekday.merge(weekday, (long) session.studySeconds(), Long::sum);
        });

        return IntStream.range(0, 7)
                .mapToObj(i -> {
                    int seconds = secondsByWeekday.getOrDefault(i, 0L).intValue();
                    return new WeeklyStudyResponse(i, WEEKDAY_LABELS[i], seconds / 60, seconds);
                })
                .toList();
    }
}
