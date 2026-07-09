package com.eqf.dto;

public class CastVoteRequest {
    private Short value;
    private String comment;

    public Short getValue() { return value; }
    public void setValue(Short value) { this.value = value; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
