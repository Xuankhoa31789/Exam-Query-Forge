package com.eqf.controller;

import com.eqf.dto.AnalyzeRequest;
import com.eqf.service.AiAnalysisService;
import com.eqf.service.DifficultyAnalyzer;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiAnalysisService aiAnalysisService;

    public AiController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    /** Gợi ý độ khó từ nội dung thô (không lưu). Dùng cho nút Tab trên form. */
    @PostMapping("/analyze")
    public Map<String, Object> analyze(@RequestBody AnalyzeRequest req) {
        DifficultyAnalyzer.Result r = aiAnalysisService.analyzeContent(req.getContent(), req.getSubjectId());
        return Map.of(
                "difficulty", r.difficulty().name(),
                "reasoning", r.reasoning(),
                "modelName", r.modelName()
        );
    }

    /** Phân tích một câu hỏi đã có trong pool và lưu lại kết quả. */
    @PostMapping("/questions/{id}/analyze")
    public Map<String, Object> analyzeQuestion(@PathVariable Long id) {
        DifficultyAnalyzer.Result r = aiAnalysisService.analyzeAndStore(id);
        return Map.of(
                "questionId", id,
                "difficulty", r.difficulty().name(),
                "reasoning", r.reasoning(),
                "modelName", r.modelName()
        );
    }
}
