package com.pokemo.quiz.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wrong_answer_notes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"}))
public class WrongAnswerNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private int missCount;

    private Long lastAttemptId;

    @Column(nullable = false)
    private OffsetDateTime lastMissedAt;

    @Column(length = 200)
    private String concept;

    protected WrongAnswerNote() {
    }

    public WrongAnswerNote(Long userId, Long questionId, Long lastAttemptId, String concept) {
        this.userId = userId;
        this.questionId = questionId;
        this.missCount = 1;
        this.lastAttemptId = lastAttemptId;
        this.lastMissedAt = OffsetDateTime.now();
        this.concept = concept;
    }

    public Long id() { return id; }
    public Long userId() { return userId; }
    public Long questionId() { return questionId; }
    public int missCount() { return missCount; }
    public Long lastAttemptId() { return lastAttemptId; }
    public OffsetDateTime lastMissedAt() { return lastMissedAt; }
    public String concept() { return concept; }

    public void incrementMiss(Long attemptId) {
        this.missCount++;
        this.lastAttemptId = attemptId;
        this.lastMissedAt = OffsetDateTime.now();
    }
}
