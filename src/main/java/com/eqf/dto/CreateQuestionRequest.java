package com.eqf.dto;

import java.util.ArrayList;
import java.util.List;

import com.eqf.model.DifficultyLevel;
import com.eqf.model.QuestionType;

/** Tạm thời nhận authorId trong request. TODO: lấy từ user đăng nhập khi có auth. */
public class CreateQuestionRequest {
    private Long subjectId;
    private Long chapterId;          // optional
    private Long authorId;
    private String content;
    private QuestionType questionType;
    private DifficultyLevel difficulty;
    private List<OptionDto> options = new ArrayList<>();

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getChapterId() { return chapterId; }
    public void setChapterId(Long chapterId) { this.chapterId = chapterId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public DifficultyLevel getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; }

    public List<OptionDto> getOptions() { return options; }
    public void setOptions(List<OptionDto> options) { this.options = options; }
}
