package com.eqf.controller;

import com.eqf.dto.CandidatePullResponse;
import com.eqf.dto.CreateExamRequest;
import com.eqf.dto.ExamCandidateResponse;
import com.eqf.dto.ExamResponse;
import com.eqf.service.ExamService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {
    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    /** Tạo kỳ thi kèm toàn bộ ma trận difficulty/requiredCount. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExamResponse create(@RequestBody CreateExamRequest request) {
        ExamService.ExamDetails details = examService.create(request);
        return ExamResponse.from(details.exam(), details.matrix(), details.candidateCount());
    }

    /** Xem thông tin kỳ thi, ma trận và tổng số ứng viên đã rút. */
    @GetMapping("/{id}")
    public ExamResponse getDetails(@PathVariable Long id) {
        ExamService.ExamDetails details = examService.getDetails(id);
        return ExamResponse.from(details.exam(), details.matrix(), details.candidateCount());
    }

    /** Kích hoạt rút ứng viên; mặc định loại câu đã dùng trong 365 ngày gần nhất. */
    @PostMapping("/{id}/candidates/pull")
    public CandidatePullResponse pullCandidates(
            @PathVariable Long id,
            @RequestParam(defaultValue = "365") int cooldownDays) {
        ExamService.CandidatePullResult result = examService.pullCandidates(id, cooldownDays);
        return CandidatePullResponse.from(
                result.exam(),
                result.candidates(),
                result.countByDifficulty(),
                result.cooldownDays()
        );
    }

    /** Liệt kê tập câu hỏi ứng viên của một kỳ thi. */
    @GetMapping("/{id}/candidates")
    public List<ExamCandidateResponse> listCandidates(@PathVariable Long id) {
        return examService.listCandidates(id).stream()
                .map(ExamCandidateResponse::from)
                .toList();
    }
}
