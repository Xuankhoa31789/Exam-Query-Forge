package com.eqf.repository;

import com.eqf.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByCandidateIdAndVoterId(Long candidateId, Long voterId);

    @Query("""
            SELECT COALESCE(SUM(v.value), 0)
            FROM Vote v
            WHERE v.candidate.id = :candidateId
            """)
    Long totalScoreByCandidateId(@Param("candidateId") Long candidateId);

    @Query("""
            SELECT c.id AS candidateId, COALESCE(SUM(v.value), 0) AS totalScore
            FROM ExamCandidate c
            LEFT JOIN Vote v ON v.candidate = c
            WHERE c.exam.id = :examId
            GROUP BY c.id
            """)
    List<CandidateScore> totalScoresByExamId(@Param("examId") Long examId);

    @Query("""
            SELECT v
            FROM Vote v
            JOIN FETCH v.candidate c
            WHERE c.exam.id = :examId
              AND v.voter.id = :voterId
            """)
    List<Vote> findByExamIdAndVoterId(@Param("examId") Long examId,
                                      @Param("voterId") Long voterId);

    interface CandidateScore {
        Long getCandidateId();
        Long getTotalScore();
    }
}
