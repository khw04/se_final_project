package com.pokemo.quiz.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private Long subjectId;

    @Column(nullable = false)
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private GeneratedBy generatedBy;

    private Long generatedFromNoteId;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    protected Quiz() {
    }

    public Quiz(String title, Long subjectId, Long createdBy, GeneratedBy generatedBy) {
        this.title = title;
        this.subjectId = subjectId;
        this.createdBy = createdBy;
        this.generatedBy = generatedBy;
        this.createdAt = OffsetDateTime.now();
    }

    public Long id() { return id; }
    public String title() { return title; }
    public Long subjectId() { return subjectId; }
    public Long createdBy() { return createdBy; }
    public GeneratedBy generatedBy() { return generatedBy; }
    public Long generatedFromNoteId() { return generatedFromNoteId; }
    public OffsetDateTime createdAt() { return createdAt; }
    public List<QuizQuestion> quizQuestions() { return quizQuestions; }

    public void setGeneratedFromNoteId(Long noteId) {
        this.generatedFromNoteId = noteId;
    }

    public void addQuestion(Question question, int position) {
        quizQuestions.add(new QuizQuestion(this, question, position));
    }
}
