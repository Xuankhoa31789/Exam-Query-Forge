package com.eqf.controller;

import com.eqf.dto.CreateQuestionRequest;
import com.eqf.dto.QuestionResponse;
import com.eqf.model.DifficultyLevel;
import com.eqf.service.QuestionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /** Tạo câu hỏi mới (DRAFT). */
    @PostMapping
    public QuestionResponse create(@RequestBody CreateQuestionRequest request) {
        return QuestionResponse.from(questionService.create(request));
    }

    /** Đưa câu hỏi vào pool chung. */
    @PostMapping("/{id}/publish")
    public QuestionResponse publish(@PathVariable Long id) {
        return QuestionResponse.from(questionService.publishToPool(id));
    }

    /** Lấy chi tiết 1 câu hỏi. */
    @GetMapping("/{id}")
    public QuestionResponse getOne(@PathVariable Long id) {
        return QuestionResponse.from(questionService.getById(id));
    }

    /** Xem pool, lọc tùy chọn theo môn / chương / độ khó. */
    @GetMapping
    public List<QuestionResponse> listPool(@RequestParam(required = false) Long subjectId,
                                           @RequestParam(required = false) Long chapterId,
                                           @RequestParam(required = false) DifficultyLevel difficulty) {
        return questionService.listPool(subjectId, chapterId, difficulty).stream()
                .map(QuestionResponse::from)
                .collect(Collectors.toList());
    }
}
