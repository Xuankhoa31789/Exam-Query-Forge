package com.eqf.controller;

import com.eqf.dto.CandidatePullResponse;
import com.eqf.dto.CreateExamRequest;
import com.eqf.dto.ExamCandidateResponse;
import com.eqf.dto.ExamResponse;
import com.eqf.dto.FinalizeExamRequest;
import com.eqf.dto.VotingCandidateResponse;
import com.eqf.model.ExamStatus;
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

    /** Liệt kê kỳ thi, có thể lọc theo trạng thái. */
    @GetMapping
    public List<ExamResponse> list(@RequestParam(required = false) ExamStatus status) {
        return examService.listExams(status).stream()
                .map(details -> ExamResponse.from(details.exam(), details.matrix(), details.candidateCount()))
                .toList();
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

    @GetMapping("/{id}/candidates/voting")
    public List<VotingCandidateResponse> listVotingCandidates(@PathVariable Long id,
                                                              @RequestParam Long voterId) {
        return examService.listVotingCandidates(id, voterId).stream()
                .map(view -> VotingCandidateResponse.from(
                        view.candidate(), view.totalScore(), view.myVote()))
                .toList();
    }

    @PostMapping("/{id}/select")
    public List<ExamCandidateResponse> selectQuestions(@PathVariable Long id) {
        return examService.selectQuestions(id).selectedCandidates().stream()
                .map(ExamCandidateResponse::from)
                .toList();
    }

    @PostMapping("/{id}/finalize")
    public List<ExamCandidateResponse> finalizeExam(@PathVariable Long id,
                                                    @RequestBody FinalizeExamRequest request) {
        return examService.finalizeExam(id, request.getUserId()).selectedCandidates().stream()
                .map(ExamCandidateResponse::from)
                .toList();
    }

    @GetMapping("/{id}/final")
    public List<ExamCandidateResponse> getFinalExam(@PathVariable Long id) {
        return examService.getFinalExam(id).selectedCandidates().stream()
                .map(ExamCandidateResponse::from)
                .toList();
    }
}
