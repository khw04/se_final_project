package com.pokemo.stats.service;

import com.pokemo.quiz.domain.Quiz;
import com.pokemo.quiz.domain.QuizAttempt;
import com.pokemo.quiz.repository.QuizAttemptRepository;
import com.pokemo.quiz.repository.QuizRepository;
import com.pokemo.stats.api.AccuracyTrendResponse;
import com.pokemo.stats.api.SubjectProgressResponse;
import com.pokemo.stats.api.WeeklyStudyResponse;
import com.pokemo.study.repository.StudySessionRepository;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private static final String[] WEEKDAY_LABELS = {"일", "월", "화", "수", "목", "금", "토"};

    private final QuizAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final StudySessionRepository studySessionRepository;

    public StatsService(
            QuizAttemptRepository attemptRepository,
            QuizRepository quizRepository,
            StudySessionRepository studySessionRepository
    ) {
        this.attemptRepository = attemptRepository;
        this.quizRepository = quizRepository;
        this.studySessionRepository = studySessionRepository;
    }

    public List<AccuracyTrendResponse> getAccuracyTrend(long userId) {
        return attemptRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .filter(a -> a.completedAt() != null)
                .map(a -> new AccuracyTrendResponse(a.startedAt().toString(), a.accuracy()))
                .toList();
    }

    public List<SubjectProgressResponse> getSubjectProgress(long userId) {
        List<QuizAttempt> attempts = attemptRepository.findByUserIdOrderByStartedAtDesc(userId);
        if (attempts.isEmpty()) return List.of();

        Map<Long, Quiz> quizMap = quizRepository
                .findAllById(attempts.stream().map(QuizAttempt::quizId).distinct().toList())
                .stream().collect(Collectors.toMap(Quiz::id, q -> q));

        record SubjectStat(int total, int correct) {}

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

        return statBySubject.entrySet().stream()
                .map(e -> {
                    SubjectStat s = e.getValue();
                    int accuracy = s.total() > 0 ? (int) Math.round((double) s.correct() / s.total() * 100) : 0;
                    return new SubjectProgressResponse(e.getKey(), accuracy, s.total(), s.total());
                })
                .toList();
    }

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
