package com.eqf.repository;

import com.eqf.model.Exam;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findBySubjectIdOrderByCreatedAtDesc(Long subjectId);

    @EntityGraph(attributePaths = {"subject", "createdBy"})
    @Query("SELECT e FROM Exam e WHERE e.id = :id")
    Optional<Exam> findDetailedById(@Param("id") Long id);
}
