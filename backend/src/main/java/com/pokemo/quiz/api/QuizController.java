package com.pokemo.quiz.api;

import com.pokemo.common.CurrentUserProvider;
import com.pokemo.quiz.service.QuizService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QuizController {

    private final QuizService quizService;
    private final CurrentUserProvider currentUserProvider;

    public QuizController(QuizService quizService, CurrentUserProvider currentUserProvider) {
        this.quizService = quizService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/quizzes")
    public List<QuizResponse> listQuizzes(Principal principal) {
        long userId = currentUserProvider.userId(principal);
        return quizService.listQuizzes(userId);
    }

    @GetMapping("/quizzes/{id}")
    public QuizResponse getQuiz(@PathVariable long id, Principal principal) {
        long userId = currentUserProvider.userId(principal);
        return quizService.getQuiz(userId, id);
    }

    @PostMapping("/quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse createQuiz(@RequestBody @Valid QuizRequest request, Principal principal) {
        long userId = currentUserProvider.userId(principal);
        return quizService.createQuiz(userId, request);
    }

    @PostMapping("/quizzes/{id}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    public AttemptResponse submitAttempt(@PathVariable long id,
            @RequestBody @Valid AttemptRequest request, Principal principal) {
        long userId = currentUserProvider.userId(principal);
        return quizService.submitAttempt(userId, id, request);
    }

    @PostMapping("/quizzes/{quizId}/questions/{questionId}/check")
    public AnswerCheckResponse checkAnswer(@PathVariable long quizId, @PathVariable long questionId,
            @RequestBody @Valid AnswerCheckRequest request, Principal principal) {
        long userId = currentUserProvider.userId(principal);
        return quizService.checkAnswer(userId, quizId, questionId, request);
    }

    @GetMapping("/wrong-answers")
    public List<WrongAnswerResponse> getWrongAnswers(
            @RequestParam(defaultValue = "all") String type, Principal principal) {
        long userId = currentUserProvider.userId(principal);
        return quizService.getWrongAnswers(userId, type);
    }

    @PostMapping("/wrong-answers/retry")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse retryWeakTypes(Principal principal) {
        long userId = currentUserProvider.userId(principal);
        return quizService.retryWeakTypes(userId);
    }
}
