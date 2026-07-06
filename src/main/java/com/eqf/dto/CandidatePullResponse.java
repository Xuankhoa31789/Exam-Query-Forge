package com.eqf.dto;

import com.eqf.model.DifficultyLevel;
import com.eqf.model.Exam;
import com.eqf.model.ExamCandidate;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/** Tóm tắt kết quả kích hoạt rút câu ứng viên. */
public class CandidatePullResponse {
    private Long examId;
    private String examStatus;
    private int candidateCount;
    private int cooldownDays;
    private Map<String, Integer> countByDifficulty;

    public static CandidatePullResponse from(Exam exam,
                                             List<ExamCandidate> candidates,
                                             Map<DifficultyLevel, Integer> countByDifficulty,
                                             int cooldownDays) {
        CandidatePullResponse response = new CandidatePullResponse();
        response.examId = exam.getId();
        response.examStatus = exam.getStatus().name();
        response.candidateCount = candidates.size();
        response.cooldownDays = cooldownDays;
        response.countByDifficulty = new LinkedHashMap<>();
        for (Map.Entry<DifficultyLevel, Integer> entry : countByDifficulty.entrySet()) {
            response.countByDifficulty.put(entry.getKey().name(), entry.getValue());
        }
        return response;
    }

    public Long getExamId() { return examId; }
    public String getExamStatus() { return examStatus; }
    public int getCandidateCount() { return candidateCount; }
    public int getCooldownDays() { return cooldownDays; }
    public Map<String, Integer> getCountByDifficulty() { return countByDifficulty; }
}
