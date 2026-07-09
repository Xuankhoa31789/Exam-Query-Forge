package com.eqf.controller;

import com.eqf.dto.CreateQuestionRequest;
import com.eqf.dto.QuestionResponse;
import com.eqf.model.DifficultyLevel;
import com.eqf.security.AuthenticatedUser;
import com.eqf.service.QuestionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    /** Tạo câu hỏi mới (DRAFT). Tác giả là người đang đăng nhập (từ JWT). */
    @PostMapping
    public QuestionResponse create(@RequestBody CreateQuestionRequest request,
                                   @AuthenticationPrincipal AuthenticatedUser user) {
        return QuestionResponse.from(questionService.create(request, user.id()));
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
