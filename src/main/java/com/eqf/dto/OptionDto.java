package com.eqf.dto;

public class OptionDto {
    private String content;
    private boolean isCorrect;
    private Integer sortOrder;

    public OptionDto() {}

    public OptionDto(String content, boolean isCorrect, Integer sortOrder) {
        this.content = content;
        this.isCorrect = isCorrect;
        this.sortOrder = sortOrder;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
