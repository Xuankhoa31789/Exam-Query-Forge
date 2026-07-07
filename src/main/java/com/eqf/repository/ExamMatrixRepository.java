package com.eqf.repository;

import com.eqf.model.ExamMatrix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamMatrixRepository extends JpaRepository<ExamMatrix, Long> {
    List<ExamMatrix> findByExamId(Long examId);

    List<ExamMatrix> findByExamIdOrderByIdAsc(Long examId);
}
