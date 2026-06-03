package com.pokemo.quiz.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AttemptRequest(
    @NotNull @Size(min = 1) @Valid List<AnswerRequest> answers
) {}
