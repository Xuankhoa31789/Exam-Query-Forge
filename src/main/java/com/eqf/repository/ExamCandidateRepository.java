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

    /**
     * Nạp sẵn câu hỏi + phương án trả lời trong MỘT truy vấn.
     * Màn hình bình chọn có thể có hơn 100 ứng viên; nạp lười sẽ thành 100+ truy vấn con.
     */
    @Query("SELECT DISTINCT c FROM ExamCandidate c "
            + "JOIN FETCH c.question q "
            + "LEFT JOIN FETCH q.options "
            + "WHERE c.exam.id = :examId "
            + "ORDER BY c.id ASC")
    List<ExamCandidate> findForVotingByExamId(@Param("examId") Long examId);

    boolean existsByExamIdAndQuestionId(Long examId, Long questionId);

    long countByExamId(Long examId);
}
