package com.pokemo.quiz.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record QuizRequest(
    @NotBlank String title,
    @NotNull Long subjectId,
    @NotNull @Size(min = 1) @Valid List<QuestionRequest> questions
) {}
