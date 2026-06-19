package com.pokemo.study.service;

import com.pokemo.common.ApiException;
import com.pokemo.study.api.StudySessionRequest;
import com.pokemo.study.api.StudySessionResponse;
import com.pokemo.study.api.TodayStudySubjectResponse;
import com.pokemo.study.api.TodayStudySummaryResponse;
import com.pokemo.study.domain.StudySession;
import com.pokemo.study.repository.StudySessionRepository;
import com.pokemo.subject.repository.SubjectRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudySessionService {

  private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

  private final StudySessionRepository studySessionRepository;
  private final SubjectRepository subjectRepository;

  public StudySessionService(StudySessionRepository studySessionRepository, SubjectRepository subjectRepository) {
    this.studySessionRepository = studySessionRepository;
    this.subjectRepository = subjectRepository;
  }

  @Transactional
  @CacheEvict(value = "weeklyStudy", key = "#userId")
  public StudySessionResponse create(long userId, StudySessionRequest request) {
    boolean ownsSubject = subjectRepository.findById(request.subjectId())
        .filter(subject -> subject.userId().equals(userId))
        .isPresent();
    if (!ownsSubject) {
      throw new ApiException(HttpStatus.NOT_FOUND, "과목을 찾을 수 없습니다.");
    }

    OffsetDateTime startedAt;
    OffsetDateTime endedAt;
    try {
      startedAt = OffsetDateTime.parse(request.startedAt());
      endedAt = OffsetDateTime.parse(request.endedAt());
    } catch (DateTimeParseException exception) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "공부 시간 형식이 올바르지 않습니다.");
    }
    if (!endedAt.isAfter(startedAt)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "종료 시간은 시작 시간 이후여야 합니다.");
    }

    long seconds = Math.max(1, Duration.between(startedAt, endedAt).toSeconds());
    StudySession session = new StudySession(userId, request.subjectId(), startedAt, endedAt, (int) Math.min(seconds, 86_400));
    return StudySessionResponse.from(studySessionRepository.save(session));
  }

  @Transactional(readOnly = true)
  public TodayStudySummaryResponse todaySummary(long userId) {
    OffsetDateTime from = LocalDate.now(SEOUL).atStartOfDay(SEOUL).toOffsetDateTime();
    OffsetDateTime to = from.plusDays(1);
    List<StudySession> sessions = studySessionRepository
        .findByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThan(userId, from, to);

    Map<Long, Integer> secondsBySubject = sessions.stream()
        .collect(Collectors.groupingBy(
            StudySession::subjectId,
            Collectors.summingInt(StudySession::studySeconds)
        ));

    List<TodayStudySubjectResponse> subjects = secondsBySubject.entrySet().stream()
        .sorted(Comparator.comparingLong(Map.Entry::getKey))
        .map(entry -> new TodayStudySubjectResponse(entry.getKey(), entry.getValue()))
        .toList();

    int total = subjects.stream().mapToInt(TodayStudySubjectResponse::studySeconds).sum();
    return new TodayStudySummaryResponse(total, subjects);
  }
}
