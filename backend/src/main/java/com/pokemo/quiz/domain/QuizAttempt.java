package com.pokemo.quiz.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempts", indexes = @Index(name = "idx_quiz_attempts_user_id", columnList = "userId"))
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long quizId;

    @Column(nullable = false)
    private int totalQuestions;

    @Column(nullable = false)
    private int correctCount;

    @Column(nullable = false)
    private OffsetDateTime startedAt;

    private OffsetDateTime completedAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttemptAnswer> answers = new ArrayList<>();

    protected QuizAttempt() {
    }

    public QuizAttempt(Long userId, Long quizId, int totalQuestions) {
        this.userId = userId;
        this.quizId = quizId;
        this.totalQuestions = totalQuestions;
        this.correctCount = 0;
        this.startedAt = OffsetDateTime.now();
    }

    public Long id() { return id; }
    public Long userId() { return userId; }
    public Long quizId() { return quizId; }
    public int totalQuestions() { return totalQuestions; }
    public int correctCount() { return correctCount; }
    public OffsetDateTime startedAt() { return startedAt; }
    public OffsetDateTime completedAt() { return completedAt; }
    public List<AttemptAnswer> answers() { return answers; }

    public double accuracy() {
        if (totalQuestions == 0) return 0.0;
        return (double) correctCount / totalQuestions * 100.0;
    }

    public void complete(List<AttemptAnswer> submittedAnswers) {
        this.answers.addAll(submittedAnswers);
        submittedAnswers.forEach(a -> a.linkAttempt(this));
        this.correctCount = (int) submittedAnswers.stream().filter(AttemptAnswer::correct).count();
        this.completedAt = OffsetDateTime.now();
    }
}
