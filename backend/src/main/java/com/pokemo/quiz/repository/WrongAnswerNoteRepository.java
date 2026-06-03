package com.pokemo.quiz.repository;

import com.pokemo.quiz.domain.WrongAnswerNote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WrongAnswerNoteRepository extends JpaRepository<WrongAnswerNote, Long> {

    List<WrongAnswerNote> findByUserIdOrderByMissCountDesc(long userId);

    Optional<WrongAnswerNote> findByUserIdAndQuestionId(long userId, long questionId);
}
