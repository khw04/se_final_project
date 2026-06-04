package com.pokemo.calendar.api;

import com.pokemo.calendar.domain.CalendarEvent;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

public record CalendarEventResponse(
    Long id,
    String title,
    Long subjectId,
    String startAt,
    String endAt,
    boolean allDay,
    String type,
    RecurrenceRuleResponse recurrence,
    String reminder,
    int dDay
) {

  public record RecurrenceRuleResponse(
      String freq,
      int interval,
      List<Integer> byweekday,
      String endMode,
      Integer endCount,
      String endUntil
  ) {}

  public static CalendarEventResponse from(CalendarEvent e) {
    RecurrenceRuleResponse recurrence = null;
    if (e.recurrenceFreq() != null) {
      List<Integer> byweekday = null;
      if (e.recurrenceByweekday() != null && !e.recurrenceByweekday().isBlank()) {
        byweekday = Arrays.stream(e.recurrenceByweekday().split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .toList();
      }
      recurrence = new RecurrenceRuleResponse(
          e.recurrenceFreq(),
          e.recurrenceInterval() != null ? e.recurrenceInterval() : 1,
          byweekday,
          e.recurrenceEndMode(),
          e.recurrenceEndCount(),
          e.recurrenceEndUntil()
      );
    }

    LocalDate startDay = e.startAt().toLocalDate();
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    int dDay = (int) (startDay.toEpochDay() - today.toEpochDay());

    return new CalendarEventResponse(
        e.id(),
        e.title(),
        e.subjectId(),
        e.startAt().toString(),
        e.endAt() != null ? e.endAt().toString() : null,
        e.allDay(),
        e.type().name().toLowerCase(),
        recurrence,
        e.reminder(),
        dDay
    );
  }
}
