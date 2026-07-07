package com.eqf.repository;

import com.eqf.model.DifficultyLevel;
import com.eqf.model.Question;
import com.eqf.model.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Tìm câu hỏi theo bộ lọc (mọi tham số đều có thể null = bỏ qua).
     * Đây là truy vấn nền cho cả "xem pool" lẫn "rút câu ứng viên" sau này.
     */
    @Query("""
            SELECT q FROM Question q
            WHERE (:subjectId IS NULL OR q.subject.id = :subjectId)
              AND (:chapterId IS NULL OR q.chapter.id = :chapterId)
              AND (:difficulty IS NULL OR q.difficulty = :difficulty)
              AND q.status = :status
            ORDER BY q.createdAt DESC
            """)
    List<Question> search(@Param("subjectId") Long subjectId,
                          @Param("chapterId") Long chapterId,
                          @Param("difficulty") DifficultyLevel difficulty,
                          @Param("status") QuestionStatus status);

    /**
     * Rút câu ứng viên theo môn và độ khó. Câu chưa từng dùng được ưu tiên trước,
     * sau đó đến câu có lần sử dụng gần nhất lâu nhất.
     */
    @Query("""
            SELECT q
            FROM Question q
            LEFT JOIN QuestionUsageHistory usage ON usage.question = q
            WHERE q.subject.id = :subjectId
              AND q.difficulty = :difficulty
              AND q.status = com.eqf.model.QuestionStatus.IN_POOL
              AND NOT EXISTS (
                  SELECT recentUsage.id
                  FROM QuestionUsageHistory recentUsage
                  WHERE recentUsage.question = q
                    AND recentUsage.usedAt >= :cooldownSince
              )
            GROUP BY q
            ORDER BY
              CASE WHEN MAX(usage.usedAt) IS NULL THEN 0 ELSE 1 END ASC,
              MAX(usage.usedAt) ASC,
              q.createdAt ASC,
              q.id ASC
            """)
    List<Question> findEligibleCandidates(@Param("subjectId") Long subjectId,
                                          @Param("difficulty") DifficultyLevel difficulty,
                                          @Param("cooldownSince") LocalDateTime cooldownSince,
                                          Pageable pageable);

    default List<Question> pullCandidates(Long subjectId,
                                          DifficultyLevel difficulty,
                                          LocalDateTime cooldownSince,
                                          int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return findEligibleCandidates(
                subjectId,
                difficulty,
                cooldownSince,
                PageRequest.of(0, limit)
        );
    }
}
