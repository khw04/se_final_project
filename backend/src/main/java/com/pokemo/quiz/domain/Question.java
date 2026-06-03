package com.pokemo.quiz.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private QuestionType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(columnDefinition = "TEXT")
    private String choicesJson;

    private Integer correctIndex;

    @Column(length = 500)
    private String correctText;

    private Boolean correctBool;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Column(nullable = false)
    private Long subjectId;

    @Column(columnDefinition = "TEXT")
    private String conceptTagsJson;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    protected Question() {
    }

    public Question(QuestionType type, String text, String explanation, Difficulty difficulty,
            Long subjectId, Long createdBy) {
        this.type = type;
        this.text = text;
        this.explanation = explanation;
        this.difficulty = difficulty;
        this.subjectId = subjectId;
        this.createdBy = createdBy;
        this.createdAt = OffsetDateTime.now();
    }

    public Long id() { return id; }
    public QuestionType type() { return type; }
    public String text() { return text; }
    public String choicesJson() { return choicesJson; }
    public Integer correctIndex() { return correctIndex; }
    public String correctText() { return correctText; }
    public Boolean correctBool() { return correctBool; }
    public String explanation() { return explanation; }
    public Difficulty difficulty() { return difficulty; }
    public Long subjectId() { return subjectId; }
    public String conceptTagsJson() { return conceptTagsJson; }
    public Long createdBy() { return createdBy; }
    public OffsetDateTime createdAt() { return createdAt; }

    public void setMcqAnswer(String choicesJson, int correctIndex) {
        this.choicesJson = choicesJson;
        this.correctIndex = correctIndex;
    }

    public void setShortAnswer(String correctText) {
        this.correctText = correctText;
    }

    public void setOxAnswer(boolean correctBool) {
        this.correctBool = correctBool;
    }

    public void setConceptTagsJson(String conceptTagsJson) {
        this.conceptTagsJson = conceptTagsJson;
    }
}
