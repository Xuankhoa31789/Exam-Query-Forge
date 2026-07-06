package com.eqf.service;

import com.eqf.model.DifficultyLevel;
import org.springframework.stereotype.Component;

/**
 * Bản giả lập (nhịp 1) — đoán độ khó bằng heuristic đơn giản dựa trên
 * từ khóa + độ dài. CHƯA phải AI thật; chỉ để dựng & demo luồng.
 * Sẽ được thay bằng lệnh gọi LLM thật ở nhịp 2 (khi có API key).
 */
@Component
public class StubDifficultyAnalyzer implements DifficultyAnalyzer {

    private static final String MODEL = "stub-heuristic-v1";

    @Override
    public Result analyze(String content, String subjectName) {
        String c = content == null ? "" : content.toLowerCase();
        int len = c.length();

        String matched;
        DifficultyLevel level;

        if (containsAny(c, "chứng minh", "tối ưu", "thiết kế", "đánh giá", "phản biện", "tổng hợp")) {
            level = DifficultyLevel.HIGH_APPLICATION;
            matched = "có yêu cầu bậc cao (chứng minh / đánh giá / thiết kế)";
        } else if (containsAny(c, "tính", "giải", "tìm", "áp dụng", "vận dụng", "so sánh", "phân tích")) {
            level = DifficultyLevel.APPLICATION;
            matched = "yêu cầu thao tác / vận dụng (tính, giải, so sánh, phân tích)";
        } else if (containsAny(c, "vì sao", "tại sao", "giải thích", "trình bày", "mô tả", "phân biệt")) {
            level = DifficultyLevel.COMPREHENSION;
            matched = "yêu cầu hiểu & giải thích (vì sao, mô tả, phân biệt)";
        } else if (containsAny(c, "là gì", "định nghĩa", "kể tên", "nêu", "liệt kê", "cho biết")) {
            level = DifficultyLevel.RECOGNITION;
            matched = "chỉ yêu cầu nhận biết / nhắc lại (định nghĩa, kể tên, nêu)";
        } else {
            // không khớp từ khóa -> đoán theo độ dài
            if (len > 220) { level = DifficultyLevel.APPLICATION; matched = "câu dài, nhiều dữ kiện"; }
            else { level = DifficultyLevel.COMPREHENSION; matched = "không có dấu hiệu rõ, đặt mức trung bình"; }
        }

        String reasoning = "Dựa trên dấu hiệu văn bản: " + matched
                + " (độ dài " + len + " ký tự). Đây là phân tích thử nghiệm, "
                + "hãy xem như gợi ý và tự điều chỉnh nếu cần.";

        return new Result(level, reasoning, MODEL);
    }

    private boolean containsAny(String text, String... keys) {
        for (String k : keys) if (text.contains(k)) return true;
        return false;
    }
}
