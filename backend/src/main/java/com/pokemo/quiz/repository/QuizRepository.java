package com.pokemo.quiz.repository;

import com.pokemo.quiz.domain.Quiz;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @EntityGraph(attributePaths = {"quizQuestions", "quizQuestions.question"})
    List<Quiz> findByCreatedByOrderByCreatedAtDesc(long userId);
}
