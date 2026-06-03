package com.pokemo.quiz.repository;

import com.pokemo.quiz.domain.Question;
import com.pokemo.quiz.domain.QuestionType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByCreatedBy(long userId);

    List<Question> findBySubjectIdAndCreatedBy(long subjectId, long userId);

    List<Question> findByTypeAndCreatedBy(QuestionType type, long userId);
}
