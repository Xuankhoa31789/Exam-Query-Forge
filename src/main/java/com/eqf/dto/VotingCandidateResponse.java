package com.eqf.dto;

import com.eqf.model.ExamCandidate;
import com.eqf.model.Question;
import com.eqf.model.Vote;

import java.time.LocalDateTime;

public class VotingCandidateResponse {
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
    private int totalScore;
    private Short myVoteValue;
    private String myVoteComment;

    public static VotingCandidateResponse from(ExamCandidate candidate, int totalScore, Vote myVote) {
        Question question = candidate.getQuestion();
        VotingCandidateResponse response = new VotingCandidateResponse();
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
        response.totalScore = totalScore;
        if (myVote != null) {
            response.myVoteValue = myVote.getValue();
            response.myVoteComment = myVote.getComment();
        }
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
    public int getTotalScore() { return totalScore; }
    public Short getMyVoteValue() { return myVoteValue; }
    public String getMyVoteComment() { return myVoteComment; }
}
