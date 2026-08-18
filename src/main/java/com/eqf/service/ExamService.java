package com.eqf.service;

import com.eqf.dto.CreateExamRequest;
import com.eqf.exception.ForbiddenException;
import com.eqf.dto.ExamMatrixItemRequest;
import com.eqf.model.*;
import com.eqf.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExamService {
    public static final BigDecimal DEFAULT_CANDIDATE_MULTIPLIER = new BigDecimal("2.5");

    private final ExamRepository examRepository;
    private final ExamMatrixRepository examMatrixRepository;
    private final ExamCandidateRepository examCandidateRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final QuestionUsageHistoryRepository questionUsageHistoryRepository;

    public ExamService(ExamRepository examRepository,
                       ExamMatrixRepository examMatrixRepository,
                       ExamCandidateRepository examCandidateRepository,
                       QuestionRepository questionRepository,
                       SubjectRepository subjectRepository,
                       UserRepository userRepository,
                       VoteRepository voteRepository,
                       QuestionUsageHistoryRepository questionUsageHistoryRepository) {
        this.examRepository = examRepository;
        this.examMatrixRepository = examMatrixRepository;
        this.examCandidateRepository = examCandidateRepository;
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.questionUsageHistoryRepository = questionUsageHistoryRepository;
    }

    /** Tạo kỳ thi và toàn bộ ma trận trong cùng một transaction. createdById lấy từ JWT. */
    @Transactional
    public ExamDetails create(CreateExamRequest request, Long createdById) {
        if (createdById == null) {
            throw new IllegalArgumentException("Người tạo là bắt buộc");
        }
        validateCreateRequest(request);

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy bộ môn id=" + request.getSubjectId()));
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người tạo id=" + createdById));

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

    @Transactional(readOnly = true)
    public List<ExamDetails> listExams(ExamStatus status) {
        List<Exam> exams = status == null
                ? examRepository.findAllByOrderByCreatedAtDesc()
                : examRepository.findByStatusOrderByCreatedAtDesc(status);
        return exams.stream()
                .map(exam -> new ExamDetails(
                        exam,
                        examMatrixRepository.findByExamIdOrderByIdAsc(exam.getId()),
                        examCandidateRepository.countByExamId(exam.getId())))
                .toList();
    }

    /**
     * Với mỗi rổ độ khó, rút ceil(requiredCount * multiplier) câu và lưu dưới
     * trạng thái CANDIDATE. Nếu một rổ thiếu câu, toàn bộ transaction được rollback.
     */
    @Transactional
    public CandidatePullResult pullCandidates(Long examId, int cooldownDays, Long userId, UserRole role) {
        if (cooldownDays < 0) {
            throw new IllegalArgumentException("cooldownDays không được âm");
        }

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỳ thi id=" + examId));
        requireExamManager(exam, userId, role, "rút câu ứng viên");
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

    @Transactional(readOnly = true)
    public List<VotingCandidateView> listVotingCandidates(Long examId, Long voterId) {
        if (voterId == null) {
            throw new IllegalArgumentException("voterId la bat buoc");
        }
        if (!examRepository.existsById(examId)) {
            throw new IllegalArgumentException("Khong tim thay ky thi id=" + examId);
        }

        Map<Long, Integer> scoreByCandidate = scoreByCandidate(examId);
        Map<Long, Vote> myVotes = voteRepository.findByExamIdAndVoterId(examId, voterId).stream()
                .collect(Collectors.toMap(vote -> vote.getCandidate().getId(), Function.identity()));

        return examCandidateRepository.findByExamIdOrderByIdAsc(examId).stream()
                .map(candidate -> new VotingCandidateView(
                        candidate,
                        scoreByCandidate.getOrDefault(candidate.getId(), 0),
                        myVotes.get(candidate.getId())))
                .toList();
    }

    @Transactional
    public SelectionResult selectQuestions(Long examId, Long userId, UserRole role) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay ky thi id=" + examId));
        requireExamManager(exam, userId, role, "chọn câu theo điểm");
        if (exam.getStatus() != ExamStatus.REVIEW) {
            throw new IllegalArgumentException("Chi ky thi REVIEW moi duoc chon cau theo diem");
        }

        List<ExamMatrix> matrix = examMatrixRepository.findByExamIdOrderByIdAsc(examId);
        if (matrix.isEmpty()) {
            throw new IllegalArgumentException("Ky thi chua co ma tran");
        }

        List<ExamCandidate> candidates = examCandidateRepository.findByExamIdOrderByIdAsc(examId);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Ky thi chua co candidate");
        }

        Map<Long, Integer> scoreByCandidate = scoreByCandidate(examId);
        Map<DifficultyLevel, List<ExamCandidate>> candidatesByDifficulty = candidates.stream()
                .collect(Collectors.groupingBy(candidate -> candidate.getQuestion().getDifficulty()));
        Set<Long> selectedIds = new HashSet<>();

        for (ExamMatrix row : matrix) {
            List<ExamCandidate> bucket = new ArrayList<>(
                    candidatesByDifficulty.getOrDefault(row.getDifficulty(), List.of()));
            if (bucket.size() < row.getRequiredCount()) {
                throw new IllegalArgumentException(
                        "Khong du candidate " + row.getDifficulty()
                                + ": can " + row.getRequiredCount() + " nhung chi co " + bucket.size());
            }
            bucket.sort(Comparator
                    .comparingInt((ExamCandidate candidate) ->
                            scoreByCandidate.getOrDefault(candidate.getId(), 0))
                    .reversed()
                    .thenComparing(ExamCandidate::getId));
            bucket.stream()
                    .limit(row.getRequiredCount())
                    .forEach(candidate -> selectedIds.add(candidate.getId()));
        }

        for (ExamCandidate candidate : candidates) {
            candidate.setStatus(selectedIds.contains(candidate.getId())
                    ? CandidateStatus.SELECTED
                    : CandidateStatus.REJECTED);
        }

        List<ExamCandidate> saved = examCandidateRepository.saveAll(candidates);
        return new SelectionResult(
                exam,
                saved.stream()
                        .filter(candidate -> candidate.getStatus() == CandidateStatus.SELECTED)
                        .toList()
        );
    }

    @Transactional
    public FinalExamResult finalizeExam(Long examId, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId la bat buoc");
        }
        Exam exam = findDetailedExam(examId);
        if (!exam.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Chi nguoi tao ky thi moi duoc chot de");
        }
        if (exam.getStatus() != ExamStatus.REVIEW) {
            throw new IllegalArgumentException("Chi ky thi REVIEW moi duoc chot de");
        }

        List<ExamCandidate> selected = examCandidateRepository.findByExamIdAndStatusOrderByIdAsc(
                examId, CandidateStatus.SELECTED);
        validateSelectedMatrix(examId, selected);

        LocalDateTime usedAt = LocalDateTime.now();
        List<QuestionUsageHistory> historyRows = selected.stream()
                .filter(candidate -> !questionUsageHistoryRepository.existsByQuestionIdAndExamId(
                        candidate.getQuestion().getId(), examId))
                .map(candidate -> new QuestionUsageHistory(candidate.getQuestion(), exam, usedAt))
                .toList();
        questionUsageHistoryRepository.saveAll(historyRows);

        exam.setStatus(ExamStatus.FINALIZED);
        examRepository.save(exam);
        return new FinalExamResult(exam, selected);
    }

    @Transactional(readOnly = true)
    public FinalExamResult getFinalExam(Long examId, Long userId, UserRole role) {
        Exam exam = findDetailedExam(examId);
        requireExamManager(exam, userId, role, "xem đề cuối cùng");
        if (exam.getStatus() != ExamStatus.FINALIZED) {
            throw new IllegalArgumentException("Chi xem duoc de cuoi sau khi ky thi da FINALIZED");
        }
        return new FinalExamResult(
                exam,
                examCandidateRepository.findByExamIdAndStatusOrderByIdAsc(examId, CandidateStatus.SELECTED)
        );
    }

    /**
     * Chỉ người tạo kỳ thi (hoặc ADMIN) mới được thao tác/đọc dữ liệu nhạy cảm của kỳ thi.
     * Đây là chốt chặn giữ đúng nguyên tắc: giáo viên bình chọn trên pool rộng
     * nhưng KHÔNG được biết câu nào lọt vào đề cuối cùng.
     */
    private void requireExamManager(Exam exam, Long userId, UserRole role, String action) {
        if (userId == null) {
            throw new ForbiddenException("Cần đăng nhập để " + action);
        }
        if (role == UserRole.ADMIN) {
            return;
        }
        if (!exam.getCreatedBy().getId().equals(userId)) {
            throw new ForbiddenException("Chỉ người tạo kỳ thi mới được " + action);
        }
    }

    private Exam findDetailedExam(Long examId) {
        return examRepository.findDetailedById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỳ thi id=" + examId));
    }

    private Map<Long, Integer> scoreByCandidate(Long examId) {
        return voteRepository.totalScoresByExamId(examId).stream()
                .collect(Collectors.toMap(
                        VoteRepository.CandidateScore::getCandidateId,
                        score -> score.getTotalScore().intValue()
                ));
    }

    private void validateSelectedMatrix(Long examId, List<ExamCandidate> selected) {
        List<ExamMatrix> matrix = examMatrixRepository.findByExamIdOrderByIdAsc(examId);
        if (matrix.isEmpty()) {
            throw new IllegalArgumentException("Ky thi chua co ma tran");
        }

        Map<DifficultyLevel, Long> selectedByDifficulty = selected.stream()
                .collect(Collectors.groupingBy(
                        candidate -> candidate.getQuestion().getDifficulty(),
                        Collectors.counting()));

        int requiredTotal = 0;
        for (ExamMatrix row : matrix) {
            requiredTotal += row.getRequiredCount();
            long selectedCount = selectedByDifficulty.getOrDefault(row.getDifficulty(), 0L);
            if (selectedCount != row.getRequiredCount()) {
                throw new IllegalArgumentException(
                        "Chua select du " + row.getDifficulty()
                                + ": can " + row.getRequiredCount() + " nhung dang co " + selectedCount);
            }
        }
        if (selected.size() != requiredTotal) {
            throw new IllegalArgumentException("So cau SELECTED khong khop tong ma tran");
        }
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

    public record VotingCandidateView(ExamCandidate candidate, int totalScore, Vote myVote) {}

    public record SelectionResult(Exam exam, List<ExamCandidate> selectedCandidates) {}

    public record FinalExamResult(Exam exam, List<ExamCandidate> selectedCandidates) {}
}
