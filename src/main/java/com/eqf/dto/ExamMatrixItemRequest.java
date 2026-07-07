package com.eqf.dto;

import com.eqf.model.DifficultyLevel;

/** Một dòng difficulty + requiredCount trong ma trận đề. */
public class ExamMatrixItemRequest {
    private DifficultyLevel difficulty;
    private Integer requiredCount;

    public ExamMatrixItemRequest() {}

    public ExamMatrixItemRequest(DifficultyLevel difficulty, Integer requiredCount) {
        this.difficulty = difficulty;
        this.requiredCount = requiredCount;
    }

    public DifficultyLevel getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; }
    public Integer getRequiredCount() { return requiredCount; }
    public void setRequiredCount(Integer requiredCount) { this.requiredCount = requiredCount; }
}
