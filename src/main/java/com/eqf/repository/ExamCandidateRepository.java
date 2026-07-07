package com.eqf.repository;

import com.eqf.model.ExamCandidate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamCandidateRepository extends JpaRepository<ExamCandidate, Long> {
    List<ExamCandidate> findByExamId(Long examId);

    @EntityGraph(attributePaths = {"exam", "question", "question.author"})
    List<ExamCandidate> findByExamIdOrderByIdAsc(Long examId);

    boolean existsByExamIdAndQuestionId(Long examId, Long questionId);

    long countByExamId(Long examId);
}
