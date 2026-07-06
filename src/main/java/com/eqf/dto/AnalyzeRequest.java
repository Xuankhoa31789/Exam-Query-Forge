package com.eqf.dto;

/** Yêu cầu phân tích độ khó từ nội dung thô (dùng cho ô nhập trên giao diện). */
public class AnalyzeRequest {
    private String content;
    private Long subjectId;   // optional — chỉ để cung cấp ngữ cảnh môn học

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
}
