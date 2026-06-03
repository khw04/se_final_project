package com.pokemo.quiz.repository;

import com.pokemo.quiz.domain.Quiz;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCreatedByOrderByCreatedAtDesc(long userId);
}
