package com.pokemo.calendar.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CalendarEventRequest(
    @NotBlank @Size(max = 200) String title,
    Long subjectId,
    @NotNull String startAt,
    String endAt,
    boolean allDay,
    @NotBlank String type,
    RecurrenceRuleRequest recurrence,
    String reminder
) {
  public record RecurrenceRuleRequest(
      String freq,
      int interval,
      List<Integer> byweekday,
      String endMode,
      Integer endCount,
      String endUntil
  ) {}
}
