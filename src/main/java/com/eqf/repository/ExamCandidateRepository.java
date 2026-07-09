package com.eqf.repository;

import com.eqf.model.CandidateStatus;
import com.eqf.model.ExamCandidate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamCandidateRepository extends JpaRepository<ExamCandidate, Long> {
    List<ExamCandidate> findByExamId(Long examId);

    @EntityGraph(attributePaths = {"exam", "question", "question.author"})
    List<ExamCandidate> findByExamIdOrderByIdAsc(Long examId);

    @EntityGraph(attributePaths = {"exam", "question", "question.author"})
    @Query("SELECT c FROM ExamCandidate c WHERE c.id = :id")
    Optional<ExamCandidate> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"exam", "question", "question.author"})
    List<ExamCandidate> findByExamIdAndStatusOrderByIdAsc(Long examId, CandidateStatus status);

    boolean existsByExamIdAndQuestionId(Long examId, Long questionId);

    long countByExamId(Long examId);
}
