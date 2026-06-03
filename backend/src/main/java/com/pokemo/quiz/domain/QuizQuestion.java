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
@Table(name = "quiz_questions")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Question question;

    @Column(nullable = false)
    private int position;

    protected QuizQuestion() {
    }

    public QuizQuestion(Quiz quiz, Question question, int position) {
        this.quiz = quiz;
        this.question = question;
        this.position = position;
    }

    public Long id() { return id; }
    public Quiz quiz() { return quiz; }
    public Question question() { return question; }
    public int position() { return position; }
}
