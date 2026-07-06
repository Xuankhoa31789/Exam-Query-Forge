package com.eqf.repository;

import com.eqf.model.AiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {
    Optional<AiAnalysis> findTopByQuestionIdOrderByAnalyzedAtDesc(Long questionId);
}
