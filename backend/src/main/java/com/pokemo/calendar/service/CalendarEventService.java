package com.pokemo.calendar.service;

import com.pokemo.calendar.api.CalendarEventRequest;
import com.pokemo.calendar.api.CalendarEventResponse;
import com.pokemo.calendar.domain.CalendarEvent;
import com.pokemo.calendar.domain.CalendarEventType;
import com.pokemo.calendar.repository.CalendarEventRepository;
import com.pokemo.common.ApiException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalendarEventService {

  private final CalendarEventRepository calendarEventRepository;

  public CalendarEventService(CalendarEventRepository calendarEventRepository) {
    this.calendarEventRepository = calendarEventRepository;
  }

  @Transactional(readOnly = true)
  public List<CalendarEventResponse> getEvents(Long userId, String from, String to) {
    OffsetDateTime fromDt = from != null ? OffsetDateTime.parse(from) : null;
    OffsetDateTime toDt = to != null ? OffsetDateTime.parse(to) : null;
    return calendarEventRepository.findByUserIdAndRange(userId, fromDt, toDt)
        .stream()
        .map(CalendarEventResponse::from)
        .toList();
  }

  @Transactional
  @CacheEvict(value = "recommend", key = "#userId")
  public CalendarEventResponse create(Long userId, CalendarEventRequest request) {
    CalendarEvent event = new CalendarEvent(
        userId,
        request.title(),
        request.subjectId(),
        OffsetDateTime.parse(request.startAt()),
        request.endAt() != null ? OffsetDateTime.parse(request.endAt()) : null,
        request.allDay(),
        parseType(request.type()),
        recurrenceFreq(request),
        recurrenceInterval(request),
        recurrenceByweekday(request),
        recurrenceEndMode(request),
        recurrenceEndCount(request),
        recurrenceEndUntil(request),
        request.reminder()
    );
    return CalendarEventResponse.from(calendarEventRepository.save(event));
  }

  @Transactional
  @CacheEvict(value = "recommend", key = "#userId")
  public CalendarEventResponse update(Long userId, Long id, CalendarEventRequest request) {
    CalendarEvent event = findOwned(userId, id);
    event.update(
        request.title(),
        request.subjectId(),
        OffsetDateTime.parse(request.startAt()),
        request.endAt() != null ? OffsetDateTime.parse(request.endAt()) : null,
        request.allDay(),
        parseType(request.type()),
        recurrenceFreq(request),
        recurrenceInterval(request),
        recurrenceByweekday(request),
        recurrenceEndMode(request),
        recurrenceEndCount(request),
        recurrenceEndUntil(request),
        request.reminder()
    );
    return CalendarEventResponse.from(event);
  }

  @Transactional
  @CacheEvict(value = "recommend", key = "#userId")
  public void delete(Long userId, Long id) {
    CalendarEvent event = findOwned(userId, id);
    calendarEventRepository.delete(event);
  }

  private CalendarEvent findOwned(Long userId, Long id) {
    CalendarEvent event = calendarEventRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "일정을 찾을 수 없습니다."));
    if (!event.userId().equals(userId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }
    return event;
  }

  private CalendarEventType parseType(String type) {
    try {
      return CalendarEventType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "유효하지 않은 일정 유형입니다: " + type);
    }
  }

  private String recurrenceFreq(CalendarEventRequest req) {
    return req.recurrence() != null ? req.recurrence().freq() : null;
  }

  private Integer recurrenceInterval(CalendarEventRequest req) {
    return req.recurrence() != null ? req.recurrence().interval() : null;
  }

  private String recurrenceByweekday(CalendarEventRequest req) {
    if (req.recurrence() == null || req.recurrence().byweekday() == null) return null;
    return req.recurrence().byweekday().stream()
        .map(String::valueOf)
        .collect(Collectors.joining(","));
  }

  private String recurrenceEndMode(CalendarEventRequest req) {
    return req.recurrence() != null ? req.recurrence().endMode() : null;
  }

  private Integer recurrenceEndCount(CalendarEventRequest req) {
    return req.recurrence() != null ? req.recurrence().endCount() : null;
  }

  private String recurrenceEndUntil(CalendarEventRequest req) {
    return req.recurrence() != null ? req.recurrence().endUntil() : null;
  }
}
