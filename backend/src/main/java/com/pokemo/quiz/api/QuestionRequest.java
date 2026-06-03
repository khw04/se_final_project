package com.pokemo.quiz.api;

import com.pokemo.quiz.domain.Difficulty;
import com.pokemo.quiz.domain.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record QuestionRequest(
    @NotNull QuestionType type,
    @NotBlank String text,
    List<String> choices,
    Integer correctIndex,
    String correctText,
    Boolean correctBool,
    @NotBlank String explanation,
    @NotNull Difficulty difficulty,
    @NotNull Long subjectId,
    List<String> conceptTags
) {}
