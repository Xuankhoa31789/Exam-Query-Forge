package com.eqf.dto;

public class CastVoteRequest {
    private Long voterId;
    private Short value;
    private String comment;

    public Long getVoterId() { return voterId; }
    public void setVoterId(Long voterId) { this.voterId = voterId; }
    public Short getValue() { return value; }
    public void setValue(Short value) { this.value = value; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
