package com.eqf.dto;

import com.eqf.model.Vote;

import java.time.LocalDateTime;

public class VoteResponse {
    private Long id;
    private Long candidateId;
    private Long voterId;
    private Short value;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VoteResponse from(Vote vote) {
        VoteResponse response = new VoteResponse();
        response.id = vote.getId();
        response.candidateId = vote.getCandidate().getId();
        response.voterId = vote.getVoter().getId();
        response.value = vote.getValue();
        response.comment = vote.getComment();
        response.createdAt = vote.getCreatedAt();
        response.updatedAt = vote.getUpdatedAt();
        return response;
    }

    public Long getId() { return id; }
    public Long getCandidateId() { return candidateId; }
    public Long getVoterId() { return voterId; }
    public Short getValue() { return value; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
