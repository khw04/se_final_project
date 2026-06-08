package com.pokemo.study.api;

import com.pokemo.common.CurrentUserProvider;
import com.pokemo.study.service.StudySessionService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-sessions")
public class StudySessionController {

  private final StudySessionService studySessionService;
  private final CurrentUserProvider currentUserProvider;

  public StudySessionController(StudySessionService studySessionService, CurrentUserProvider currentUserProvider) {
    this.studySessionService = studySessionService;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  StudySessionResponse create(Principal principal, @Valid @RequestBody StudySessionRequest request) {
    long userId = currentUserProvider.userId(principal);
    return studySessionService.create(userId, request);
  }

  @GetMapping("/today")
  TodayStudySummaryResponse today(Principal principal) {
    long userId = currentUserProvider.userId(principal);
    return studySessionService.todaySummary(userId);
  }
}
