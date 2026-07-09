package com.eqf.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateExamRequest {
    private String title;
    private Long subjectId;
    private Integer grade;
    private Integer totalQuestions;
    private BigDecimal candidateMultiplier;
    private LocalDate examDate;
    private List<ExamMatrixItemRequest> matrix = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public BigDecimal getCandidateMultiplier() { return candidateMultiplier; }
    public void setCandidateMultiplier(BigDecimal candidateMultiplier) { this.candidateMultiplier = candidateMultiplier; }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
    public List<ExamMatrixItemRequest> getMatrix() { return matrix; }
    public void setMatrix(List<ExamMatrixItemRequest> matrix) { this.matrix = matrix; }
}
