package com.pokemo.ai.api;

import com.pokemo.ai.service.AiService;
import com.pokemo.common.CurrentUserProvider;
import com.pokemo.quiz.api.QuizResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AiController {

  private final AiService aiService;
  private final CurrentUserProvider currentUserProvider;

  public AiController(AiService aiService, CurrentUserProvider currentUserProvider) {
    this.aiService = aiService;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping("/ai/summary")
  public AiSummaryResponse summarize(@Valid @RequestBody AiSummaryRequest request, Principal principal) {
    long userId = currentUserProvider.userId(principal);
    return aiService.summarizeNote(userId, request.noteId());
  }

  @PostMapping("/quiz/generate")
  @ResponseStatus(HttpStatus.CREATED)
  public QuizResponse generateQuiz(@Valid @RequestBody QuizGenerateRequest request, Principal principal) {
    long userId = currentUserProvider.userId(principal);
    return aiService.generateQuiz(userId, request.noteId(), request.effectiveCount());
  }

  @GetMapping("/recommend")
  public RecommendResponse recommend(Principal principal) {
    long userId = currentUserProvider.userId(principal);
    return aiService.recommend(userId);
  }
}
