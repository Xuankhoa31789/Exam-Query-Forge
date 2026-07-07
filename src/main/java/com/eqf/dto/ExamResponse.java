package com.eqf.dto;

import com.eqf.model.Exam;
import com.eqf.model.ExamMatrix;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Chi tiết kỳ thi kèm ma trận, không trả trực tiếp JPA entity. */
public class ExamResponse {
    private Long id;
    private String title;
    private Long subjectId;
    private String subjectName;
    private Integer grade;
    private Integer totalQuestions;
    private String status;
    private BigDecimal candidateMultiplier;
    private Long createdById;
    private String createdByName;
    private LocalDate examDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long candidateCount;
    private List<MatrixRow> matrix;

    public static ExamResponse from(Exam exam, List<ExamMatrix> matrix, long candidateCount) {
        ExamResponse response = new ExamResponse();
        response.id = exam.getId();
        response.title = exam.getTitle();
        response.subjectId = exam.getSubject().getId();
        response.subjectName = exam.getSubject().getName();
        response.grade = exam.getGrade();
        response.totalQuestions = exam.getTotalQuestions();
        response.status = exam.getStatus().name();
        response.candidateMultiplier = exam.getCandidateMultiplier();
        response.createdById = exam.getCreatedBy().getId();
        response.createdByName = exam.getCreatedBy().getFullName();
        response.examDate = exam.getExamDate();
        response.createdAt = exam.getCreatedAt();
        response.updatedAt = exam.getUpdatedAt();
        response.candidateCount = candidateCount;
        response.matrix = matrix.stream().map(MatrixRow::from).toList();
        return response;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Long getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public Integer getGrade() { return grade; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public String getStatus() { return status; }
    public BigDecimal getCandidateMultiplier() { return candidateMultiplier; }
    public Long getCreatedById() { return createdById; }
    public String getCreatedByName() { return createdByName; }
    public LocalDate getExamDate() { return examDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public long getCandidateCount() { return candidateCount; }
    public List<MatrixRow> getMatrix() { return matrix; }

    public static class MatrixRow {
        private Long id;
        private String difficulty;
        private Integer requiredCount;

        private static MatrixRow from(ExamMatrix matrix) {
            MatrixRow row = new MatrixRow();
            row.id = matrix.getId();
            row.difficulty = matrix.getDifficulty().name();
            row.requiredCount = matrix.getRequiredCount();
            return row;
        }

        public Long getId() { return id; }
        public String getDifficulty() { return difficulty; }
        public Integer getRequiredCount() { return requiredCount; }
    }
}
