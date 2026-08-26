package com.eqf.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class OptionDto {
    private String content;
    /**
     * Jackson suy tên khoá JSON từ getter/setter nên khoá chuẩn là "correct".
     * Giữ thêm bí danh "isCorrect" vì giao diện từng gửi tên đó, và một khoá sai
     * chỉ bị bỏ qua trong im lặng — hậu quả là câu hỏi lưu vào mà KHÔNG có đáp án
     * đúng nào, không báo lỗi gì cả. Đã từng xảy ra với toàn bộ ngân hàng câu hỏi.
     */
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
    @JsonAlias("isCorrect")
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
