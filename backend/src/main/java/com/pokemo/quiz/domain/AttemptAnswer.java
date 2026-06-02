package com.pokemo.quiz.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "attempt_answers")
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private QuizAttempt attempt;

    @Column(nullable = false)
    private Long questionId;

    @Column(length = 500)
    private String userAnswer;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private int timeSpentSec;

    protected AttemptAnswer() {
    }

    public AttemptAnswer(Long questionId, String userAnswer, boolean correct, int timeSpentSec) {
        this.questionId = questionId;
        this.userAnswer = userAnswer;
        this.correct = correct;
        this.timeSpentSec = timeSpentSec;
    }

    public Long id() { return id; }
    public QuizAttempt attempt() { return attempt; }
    public Long questionId() { return questionId; }
    public String userAnswer() { return userAnswer; }
    public boolean correct() { return correct; }
    public int timeSpentSec() { return timeSpentSec; }

    void linkAttempt(QuizAttempt attempt) {
        this.attempt = attempt;
    }
}
