package com.eqf.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Một câu hỏi được rút vào tập ứng viên của một kỳ thi. */
@Entity
@Table(
        name = "exam_candidates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "question_id"}),
        indexes = @Index(name = "idx_candidates_exam", columnList = "exam_id, status")
)
public class ExamCandidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private CandidateStatus status = CandidateStatus.CANDIDATE;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    public ExamCandidate() {}

    public ExamCandidate(Exam exam, Question question) {
        this.exam = exam;
        this.question = question;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public CandidateStatus getStatus() { return status; }
    public void setStatus(CandidateStatus status) { this.status = status; }
    public LocalDateTime getAddedAt() { return addedAt; }
}
