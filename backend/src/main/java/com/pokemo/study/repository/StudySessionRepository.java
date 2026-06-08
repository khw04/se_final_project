package com.pokemo.study.repository;

import com.pokemo.study.domain.StudySession;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

  List<StudySession> findByUserIdOrderByStartedAtDesc(long userId);

  List<StudySession> findByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
      long userId,
      OffsetDateTime from,
      OffsetDateTime to
  );
}
