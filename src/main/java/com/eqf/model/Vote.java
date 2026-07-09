package com.eqf.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "votes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"candidate_id", "voter_id"}),
        indexes = @Index(name = "idx_votes_candidate", columnList = "candidate_id")
)
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private ExamCandidate candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @Column(name = "\"value\"", nullable = false)
    private Short value;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Vote() {}

    public Vote(ExamCandidate candidate, User voter, Short value, String comment) {
        this.candidate = candidate;
        this.voter = voter;
        this.value = value;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ExamCandidate getCandidate() { return candidate; }
    public void setCandidate(ExamCandidate candidate) { this.candidate = candidate; }
    public User getVoter() { return voter; }
    public void setVoter(User voter) { this.voter = voter; }
    public Short getValue() { return value; }
    public void setValue(Short value) { this.value = value; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
