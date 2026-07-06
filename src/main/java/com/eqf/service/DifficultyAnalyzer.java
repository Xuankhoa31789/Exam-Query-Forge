package com.eqf.service;

import com.eqf.model.DifficultyLevel;

/**
 * Bộ phân tích độ khó — thiết kế dạng "cắm-thay".
 * Hiện có 1 bản giả lập (StubDifficultyAnalyzer). Khi có API key,
 * chỉ cần thêm 1 lớp mới implement interface này (gọi LLM thật)
 * và đánh dấu @Primary — KHÔNG phải sửa service/controller/giao diện.
 */
public interface DifficultyAnalyzer {

    Result analyze(String content, String subjectName);

    /** Kết quả: mức độ gợi ý + lý do + tên "model" đã dùng. */
    record Result(DifficultyLevel difficulty, String reasoning, String modelName) {}
}
