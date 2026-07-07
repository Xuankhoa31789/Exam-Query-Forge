package com.eqf.model;

import jakarta.persistence.*;

/** Số câu bắt buộc cho một mức độ khó trong ma trận của kỳ thi. */
@Entity
@Table(
        name = "exam_matrix",
        uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "difficulty"})
)
public class ExamMatrix {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private DifficultyLevel difficulty;

    @Column(name = "required_count", nullable = false)
    private Integer requiredCount;

    public ExamMatrix() {}

    public ExamMatrix(Exam exam, DifficultyLevel difficulty, Integer requiredCount) {
        this.exam = exam;
        this.difficulty = difficulty;
        this.requiredCount = requiredCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
    public DifficultyLevel getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; }
    public Integer getRequiredCount() { return requiredCount; }
    public void setRequiredCount(Integer requiredCount) { this.requiredCount = requiredCount; }
}
