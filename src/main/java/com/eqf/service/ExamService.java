package com.eqf.service;

import com.eqf.dto.CreateExamRequest;
import com.eqf.dto.ExamMatrixItemRequest;
import com.eqf.model.*;
import com.eqf.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
public class ExamService {
    public static final BigDecimal DEFAULT_CANDIDATE_MULTIPLIER = new BigDecimal("2.5");

    private final ExamRepository examRepository;
    private final ExamMatrixRepository examMatrixRepository;
    private final ExamCandidateRepository examCandidateRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public ExamService(ExamRepository examRepository,
                       ExamMatrixRepository examMatrixRepository,
                       ExamCandidateRepository examCandidateRepository,
                       QuestionRepository questionRepository,
                       SubjectRepository subjectRepository,
                       UserRepository userRepository) {
        this.examRepository = examRepository;
        this.examMatrixRepository = examMatrixRepository;
        this.examCandidateRepository = examCandidateRepository;
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    /** Tạo kỳ thi và toàn bộ ma trận trong cùng một transaction. */
    @Transactional
    public ExamDetails create(CreateExamRequest request) {
        validateCreateRequest(request);

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy bộ môn id=" + request.getSubjectId()));
        User createdBy = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người tạo id=" + request.getCreatedById()));

        Exam exam = new Exam();
        exam.setTitle(request.getTitle().trim());
        exam.setSubject(subject);
        exam.setGrade(request.getGrade());
        exam.setTotalQuestions(request.getTotalQuestions());
        exam.setCandidateMultiplier(normalizeMultiplier(request.getCandidateMultiplier()));
        exam.setCreatedBy(createdBy);
        exam.setExamDate(request.getExamDate());
        exam.setStatus(ExamStatus.DRAFT);
        exam = examRepository.save(exam);

        List<ExamMatrix> matrix = new ArrayList<>();
        for (ExamMatrixItemRequest row : request.getMatrix()) {
            matrix.add(new ExamMatrix(exam, row.getDifficulty(), row.getRequiredCount()));
        }
        matrix = examMatrixRepository.saveAll(matrix);

        return new ExamDetails(exam, matrix, 0);
    }

    @Transactional(readOnly = true)
    public ExamDetails getDetails(Long examId) {
        Exam exam = findDetailedExam(examId);
        List<ExamMatrix> matrix = examMatrixRepository.findByExamIdOrderByIdAsc(examId);
        long candidateCount = examCandidateRepository.countByExamId(examId);
        return new ExamDetails(exam, matrix, candidateCount);
    }

    /**
     * Với mỗi rổ độ khó, rút ceil(requiredCount * multiplier) câu và lưu dưới
     * trạng thái CANDIDATE. Nếu một rổ thiếu câu, toàn bộ transaction được rollback.
     */
    @Transactional
    public CandidatePullResult pullCandidates(Long examId, int cooldownDays) {
        if (cooldownDays < 0) {
            throw new IllegalArgumentException("cooldownDays không được âm");
        }

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỳ thi id=" + examId));
        if (exam.getStatus() != ExamStatus.DRAFT) {
            throw new IllegalArgumentException("Chỉ kỳ thi ở trạng thái DRAFT mới được rút câu ứng viên");
        }
        if (examCandidateRepository.countByExamId(examId) > 0) {
            throw new IllegalArgumentException("Kỳ thi đã có câu ứng viên");
        }

        List<ExamMatrix> matrix = examMatrixRepository.findByExamIdOrderByIdAsc(examId);
        if (matrix.isEmpty()) {
            throw new IllegalArgumentException("Kỳ thi chưa có ma trận");
        }

        LocalDateTime cooldownSince = LocalDateTime.now().minusDays(cooldownDays);
        List<ExamCandidate> candidatesToSave = new ArrayList<>();
        Map<DifficultyLevel, Integer> countByDifficulty = new EnumMap<>(DifficultyLevel.class);

        for (ExamMatrix row : matrix) {
            int targetCount = calculateCandidateCount(
                    row.getRequiredCount(), exam.getCandidateMultiplier());
            List<Question> questions = questionRepository.pullCandidates(
                    exam.getSubject().getId(), row.getDifficulty(), cooldownSince, targetCount);

            if (questions.size() < targetCount) {
                throw new IllegalArgumentException(
                        "Không đủ câu " + row.getDifficulty()
                                + ": cần " + targetCount + " ứng viên nhưng chỉ có " + questions.size());
            }

            for (Question question : questions) {
                candidatesToSave.add(new ExamCandidate(exam, question));
            }
            countByDifficulty.put(row.getDifficulty(), questions.size());
        }

        List<ExamCandidate> savedCandidates = examCandidateRepository.saveAll(candidatesToSave);
        exam.setStatus(ExamStatus.REVIEW);
        examRepository.save(exam);

        return new CandidatePullResult(exam, savedCandidates, countByDifficulty, cooldownDays);
    }

    @Transactional(readOnly = true)
    public List<ExamCandidate> listCandidates(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new IllegalArgumentException("Không tìm thấy kỳ thi id=" + examId);
        }
        return examCandidateRepository.findByExamIdOrderByIdAsc(examId);
    }

    private Exam findDetailedExam(Long examId) {
        return examRepository.findDetailedById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỳ thi id=" + examId));
    }

    private void validateCreateRequest(CreateExamRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu kỳ thi là bắt buộc");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Tên kỳ thi là bắt buộc");
        }
        if (request.getSubjectId() == null) {
            throw new IllegalArgumentException("Bộ môn là bắt buộc");
        }
        if (request.getCreatedById() == null) {
            throw new IllegalArgumentException("Người tạo là bắt buộc");
        }
        if (request.getTotalQuestions() == null || request.getTotalQuestions() <= 0) {
            throw new IllegalArgumentException("Tổng số câu phải lớn hơn 0");
        }
        if (request.getMatrix() == null || request.getMatrix().isEmpty()) {
            throw new IllegalArgumentException("Ma trận kỳ thi không được để trống");
        }

        EnumSet<DifficultyLevel> difficulties = EnumSet.noneOf(DifficultyLevel.class);
        long matrixTotal = 0;
        for (ExamMatrixItemRequest row : request.getMatrix()) {
            if (row == null || row.getDifficulty() == null) {
                throw new IllegalArgumentException("Mỗi dòng ma trận phải có difficulty");
            }
            if (row.getRequiredCount() == null || row.getRequiredCount() <= 0) {
                throw new IllegalArgumentException("requiredCount của ma trận phải lớn hơn 0");
            }
            if (!difficulties.add(row.getDifficulty())) {
                throw new IllegalArgumentException("Difficulty bị trùng trong ma trận: " + row.getDifficulty());
            }
            matrixTotal += row.getRequiredCount();
        }
        if (matrixTotal != request.getTotalQuestions()) {
            throw new IllegalArgumentException(
                    "Tổng requiredCount trong ma trận phải bằng totalQuestions");
        }

        normalizeMultiplier(request.getCandidateMultiplier());
    }

    private BigDecimal normalizeMultiplier(BigDecimal requestedMultiplier) {
        BigDecimal multiplier = requestedMultiplier == null
                ? DEFAULT_CANDIDATE_MULTIPLIER
                : requestedMultiplier;
        if (multiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("candidateMultiplier phải lớn hơn 0");
        }
        if (multiplier.compareTo(new BigDecimal("99.9")) > 0) {
            throw new IllegalArgumentException("candidateMultiplier không được lớn hơn 99.9");
        }
        try {
            return multiplier.setScale(1, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("candidateMultiplier chỉ được có tối đa 1 chữ số thập phân");
        }
    }

    private int calculateCandidateCount(Integer requiredCount, BigDecimal multiplier) {
        try {
            return multiplier.multiply(BigDecimal.valueOf(requiredCount))
                    .setScale(0, RoundingMode.CEILING)
                    .intValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Số câu ứng viên vượt quá giới hạn cho phép");
        }
    }

    public record ExamDetails(Exam exam, List<ExamMatrix> matrix, long candidateCount) {}

    public record CandidatePullResult(Exam exam,
                                      List<ExamCandidate> candidates,
                                      Map<DifficultyLevel, Integer> countByDifficulty,
                                      int cooldownDays) {}
}
