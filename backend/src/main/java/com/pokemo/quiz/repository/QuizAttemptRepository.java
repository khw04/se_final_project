package com.pokemo.quiz.repository;

import com.pokemo.quiz.domain.QuizAttempt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(long userId);

    List<QuizAttempt> findByUserIdAndQuizId(long userId, long quizId);
}
