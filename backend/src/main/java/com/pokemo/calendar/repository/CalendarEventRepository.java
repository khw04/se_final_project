package com.pokemo.calendar.repository;

import com.pokemo.calendar.domain.CalendarEvent;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

  @Query("SELECT e FROM CalendarEvent e WHERE e.userId = :userId " +
      "AND (:from IS NULL OR e.startAt >= :from) " +
      "AND (:to IS NULL OR e.startAt <= :to) " +
      "ORDER BY e.startAt ASC")
  List<CalendarEvent> findByUserIdAndRange(
      @Param("userId") Long userId,
      @Param("from") OffsetDateTime from,
      @Param("to") OffsetDateTime to
  );

  List<CalendarEvent> findByUserIdOrderByStartAtAsc(Long userId);

  List<CalendarEvent> findByReminderAndStartAtBetween(
      String reminder,
      OffsetDateTime from,
      OffsetDateTime to
  );
}
