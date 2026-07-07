package com.eqf.dto;

import com.eqf.model.ExamCandidate;
import com.eqf.model.Question;

import java.time.LocalDateTime;

/** Câu hỏi ứng viên trong ngữ cảnh một kỳ thi. */
public class ExamCandidateResponse {
    private Long id;
    private Long examId;
    private Long questionId;
    private String content;
    private String difficulty;
    private String questionType;
    private String status;
    private Long authorId;
    private String authorName;
    private LocalDateTime addedAt;

    public static ExamCandidateResponse from(ExamCandidate candidate) {
        Question question = candidate.getQuestion();
        ExamCandidateResponse response = new ExamCandidateResponse();
        response.id = candidate.getId();
        response.examId = candidate.getExam().getId();
        response.questionId = question.getId();
        response.content = question.getContent();
        response.difficulty = question.getDifficulty().name();
        response.questionType = question.getQuestionType().name();
        response.status = candidate.getStatus().name();
        response.authorId = question.getAuthor().getId();
        response.authorName = question.getAuthor().getFullName();
        response.addedAt = candidate.getAddedAt();
        return response;
    }

    public Long getId() { return id; }
    public Long getExamId() { return examId; }
    public Long getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public String getDifficulty() { return difficulty; }
    public String getQuestionType() { return questionType; }
    public String getStatus() { return status; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public LocalDateTime getAddedAt() { return addedAt; }
}
