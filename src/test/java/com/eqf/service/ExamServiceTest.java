package com.eqf.service;

import com.eqf.dto.CreateExamRequest;
import com.eqf.dto.ExamMatrixItemRequest;
import com.eqf.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(ExamService.class)
class ExamServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExamService examService;

    @Test
    void createsExamWithMatrixThenPullsAndListsCandidates() {
        Subject subject = entityManager.persist(new Subject("Toán service test"));
        User creator = new User("Trưởng bộ môn", "exam-service@eqf.local", "hash");
        creator.setRole(UserRole.DEPARTMENT_HEAD);
        creator.setSubject(subject);
        creator.setVerifyStatus(VerifyStatus.VERIFIED);
        creator = entityManager.persist(creator);

        Question neverUsed = entityManager.persist(question(subject, creator, "Câu chưa dùng"));
        Question usedLongAgo = entityManager.persist(question(subject, creator, "Câu dùng từ lâu"));
        Question inCooldown = entityManager.persist(question(subject, creator, "Câu đang cooldown"));

        Exam previousExam = new Exam();
        previousExam.setTitle("Đề cũ");
        previousExam.setSubject(subject);
        previousExam.setTotalQuestions(2);
        previousExam.setCreatedBy(creator);
        previousExam.setStatus(ExamStatus.PUBLISHED);
        previousExam = entityManager.persist(previousExam);

        LocalDateTime now = LocalDateTime.now();
        entityManager.persist(new QuestionUsageHistory(usedLongAgo, previousExam, now.minusDays(90)));
        entityManager.persist(new QuestionUsageHistory(inCooldown, previousExam, now.minusDays(5)));
        entityManager.flush();

        CreateExamRequest request = validRequest(subject.getId(), creator.getId());
        ExamService.ExamDetails created = examService.create(request);

        assertThat(created.exam().getStatus()).isEqualTo(ExamStatus.DRAFT);
        assertThat(created.matrix()).hasSize(1);
        assertThat(created.matrix().get(0).getRequiredCount()).isEqualTo(1);

        ExamService.CandidatePullResult pulled = examService.pullCandidates(created.exam().getId(), 30);

        assertThat(pulled.exam().getStatus()).isEqualTo(ExamStatus.REVIEW);
        assertThat(pulled.candidates()).hasSize(2);
        assertThat(pulled.countByDifficulty()).containsEntry(DifficultyLevel.RECOGNITION, 2);
        assertThat(pulled.candidates())
                .extracting(candidate -> candidate.getQuestion().getContent())
                .containsExactly("Câu chưa dùng", "Câu dùng từ lâu")
                .doesNotContain("Câu đang cooldown");
        assertThat(pulled.candidates())
                .allMatch(candidate -> candidate.getStatus() == CandidateStatus.CANDIDATE);

        ExamService.ExamDetails details = examService.getDetails(created.exam().getId());
        assertThat(details.candidateCount()).isEqualTo(2);
        assertThat(details.exam().getStatus()).isEqualTo(ExamStatus.REVIEW);
        assertThat(examService.listCandidates(created.exam().getId())).hasSize(2);

        assertThatThrownBy(() -> examService.pullCandidates(created.exam().getId(), 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DRAFT");

        assertThat(neverUsed.getId()).isNotNull();
    }

    @Test
    void rejectsMatrixWhoseTotalDoesNotMatchExamTotal() {
        Subject subject = entityManager.persist(new Subject("Lý service test"));
        User creator = new User("Người tạo", "matrix-test@eqf.local", "hash");
        creator.setSubject(subject);
        creator = entityManager.persist(creator);

        CreateExamRequest request = validRequest(subject.getId(), creator.getId());
        request.setTotalQuestions(2);

        assertThatThrownBy(() -> examService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalQuestions");
    }

    @Test
    void selectsTopVotedCandidatesPerDifficultyBucket() {
        Subject subject = entityManager.persist(new Subject("Select service test"));
        User creator = verifiedUser(subject, "select-creator@eqf.local", UserRole.DEPARTMENT_HEAD);
        User voterOne = verifiedUser(subject, "select-voter-1@eqf.local", UserRole.TEACHER);
        User voterTwo = verifiedUser(subject, "select-voter-2@eqf.local", UserRole.TEACHER);

        Question lowScore = entityManager.persist(question(subject, creator, "Low score"));
        Question highScore = entityManager.persist(question(subject, creator, "High score"));
        Question middleScore = entityManager.persist(question(subject, creator, "Middle score"));

        Exam exam = new Exam();
        exam.setTitle("Select exam");
        exam.setSubject(subject);
        exam.setTotalQuestions(2);
        exam.setCreatedBy(creator);
        exam.setStatus(ExamStatus.REVIEW);
        exam = entityManager.persist(exam);
        entityManager.persist(new ExamMatrix(exam, DifficultyLevel.RECOGNITION, 2));

        ExamCandidate lowCandidate = entityManager.persist(new ExamCandidate(exam, lowScore));
        ExamCandidate highCandidate = entityManager.persist(new ExamCandidate(exam, highScore));
        ExamCandidate middleCandidate = entityManager.persist(new ExamCandidate(exam, middleScore));

        entityManager.persist(new Vote(lowCandidate, voterOne, (short) -1, "Khong phu hop"));
        entityManager.persist(new Vote(highCandidate, voterOne, (short) 1, null));
        entityManager.persist(new Vote(highCandidate, voterTwo, (short) 1, null));
        entityManager.persist(new Vote(middleCandidate, voterOne, (short) 1, null));
        entityManager.flush();

        ExamService.SelectionResult result = examService.selectQuestions(exam.getId());

        assertThat(result.selectedCandidates())
                .extracting(candidate -> candidate.getQuestion().getContent())
                .containsExactly("High score", "Middle score");
        assertThat(lowCandidate.getStatus()).isEqualTo(CandidateStatus.REJECTED);
        assertThat(highCandidate.getStatus()).isEqualTo(CandidateStatus.SELECTED);
        assertThat(middleCandidate.getStatus()).isEqualTo(CandidateStatus.SELECTED);
    }

    private CreateExamRequest validRequest(Long subjectId, Long createdById) {
        CreateExamRequest request = new CreateExamRequest();
        request.setTitle("Đề giữa kỳ");
        request.setSubjectId(subjectId);
        request.setGrade(10);
        request.setTotalQuestions(1);
        request.setCandidateMultiplier(new BigDecimal("2.0"));
        request.setCreatedById(createdById);
        request.setMatrix(List.of(
                new ExamMatrixItemRequest(DifficultyLevel.RECOGNITION, 1)
        ));
        return request;
    }

    private Question question(Subject subject, User author, String content) {
        Question question = new Question();
        question.setSubject(subject);
        question.setAuthor(author);
        question.setContent(content);
        question.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        question.setDifficulty(DifficultyLevel.RECOGNITION);
        question.setDifficultySource(DifficultySource.AUTHOR);
        question.setStatus(QuestionStatus.IN_POOL);
        return question;
    }

    private User verifiedUser(Subject subject, String email, UserRole role) {
        User user = new User(email, email, "hash");
        user.setRole(role);
        user.setSubject(subject);
        user.setVerifyStatus(VerifyStatus.VERIFIED);
        return entityManager.persist(user);
    }
}
