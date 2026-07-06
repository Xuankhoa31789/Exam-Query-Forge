package com.eqf.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Kết quả phân tích độ khó của AI cho một câu hỏi.
 * Đây CHỈ là gợi ý + giải thích — không ghi đè questions.difficulty.
 * Quyết định cuối vẫn thuộc về giáo viên.
 */
@Entity
@Table(name = "ai_analyses")
public class AiAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_difficulty", length = 20, nullable = false)
    private DifficultyLevel suggestedDifficulty;

    @Column(columnDefinition = "TEXT")
    private String reasoning;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "analyzed_at", nullable = false, updatable = false)
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() { analyzedAt = LocalDateTime.now(); }

    public AiAnalysis() {}

    public AiAnalysis(Question question, DifficultyLevel suggestedDifficulty, String reasoning, String modelName) {
        this.question = question;
        this.suggestedDifficulty = suggestedDifficulty;
        this.reasoning = reasoning;
        this.modelName = modelName;
    }

    public Long getId() { return id; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public DifficultyLevel getSuggestedDifficulty() { return suggestedDifficulty; }
    public void setSuggestedDifficulty(DifficultyLevel d) { this.suggestedDifficulty = d; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
}
