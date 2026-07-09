package com.eqf.repository;

import com.eqf.model.QuestionUsageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionUsageHistoryRepository extends JpaRepository<QuestionUsageHistory, Long> {
    boolean existsByQuestionIdAndExamId(Long questionId, Long examId);
}
