package com.pokemo.calendar.api;

import com.pokemo.calendar.service.CalendarEventService;
import com.pokemo.common.CurrentUserProvider;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class CalendarEventController {

  private final CalendarEventService calendarEventService;
  private final CurrentUserProvider currentUserProvider;

  public CalendarEventController(
      CalendarEventService calendarEventService,
      CurrentUserProvider currentUserProvider
  ) {
    this.calendarEventService = calendarEventService;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  List<CalendarEventResponse> getEvents(
      Principal principal,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to
  ) {
    long userId = currentUserProvider.userId(principal);
    return calendarEventService.getEvents(userId, from, to);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  CalendarEventResponse create(
      Principal principal,
      @Valid @RequestBody CalendarEventRequest request
  ) {
    long userId = currentUserProvider.userId(principal);
    return calendarEventService.create(userId, request);
  }

  @PutMapping("/{id}")
  CalendarEventResponse update(
      Principal principal,
      @PathVariable Long id,
      @Valid @RequestBody CalendarEventRequest request
  ) {
    long userId = currentUserProvider.userId(principal);
    return calendarEventService.update(userId, id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(Principal principal, @PathVariable Long id) {
    long userId = currentUserProvider.userId(principal);
    calendarEventService.delete(userId, id);
  }
}
