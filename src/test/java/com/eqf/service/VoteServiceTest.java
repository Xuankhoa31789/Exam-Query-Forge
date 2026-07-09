package com.eqf.service;

import com.eqf.dto.CastVoteRequest;
import com.eqf.model.*;
import com.eqf.repository.VoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(VoteService.class)
class VoteServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VoteService voteService;

    @Autowired
    private VoteRepository voteRepository;

    @Test
    void castsAndUpdatesOneVotePerCandidateAndVoter() {
        Fixture fixture = fixture(ExamStatus.REVIEW, VerifyStatus.VERIFIED, "vote-update");

        Vote first = voteService.castVote(
                fixture.candidate().getId(),
                fixture.voter().getId(),
                voteRequest(1, null));

        assertThat(first.getValue()).isEqualTo((short) 1);
        assertThat(first.getComment()).isNull();
        assertThat(voteRepository.totalScoreByCandidateId(fixture.candidate().getId())).isEqualTo(1L);

        Vote updated = voteService.castVote(
                fixture.candidate().getId(),
                fixture.voter().getId(),
                voteRequest(0, "Can sua wording"));

        assertThat(updated.getId()).isEqualTo(first.getId());
        assertThat(updated.getValue()).isEqualTo((short) 0);
        assertThat(updated.getComment()).isEqualTo("Can sua wording");
        assertThat(voteRepository.findAll()).hasSize(1);
        assertThat(voteRepository.totalScoreByCandidateId(fixture.candidate().getId())).isEqualTo(0L);
    }

    @Test
    void requiresCommentWhenVoteIsZeroOrNegative() {
        Fixture fixture = fixture(ExamStatus.REVIEW, VerifyStatus.VERIFIED, "vote-comment");

        assertThatThrownBy(() -> voteService.castVote(
                fixture.candidate().getId(),
                fixture.voter().getId(),
                voteRequest(-1, "  ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("comment");
    }

    @Test
    void rejectsUnverifiedVoter() {
        Fixture fixture = fixture(ExamStatus.REVIEW, VerifyStatus.PENDING, "vote-unverified");

        assertThatThrownBy(() -> voteService.castVote(
                fixture.candidate().getId(),
                fixture.voter().getId(),
                voteRequest(1, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("xac minh");
    }

    @Test
    void rejectsVoteWhenExamIsNotInReview() {
        Fixture fixture = fixture(ExamStatus.DRAFT, VerifyStatus.VERIFIED, "vote-draft");

        assertThatThrownBy(() -> voteService.castVote(
                fixture.candidate().getId(),
                fixture.voter().getId(),
                voteRequest(1, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("REVIEW");
    }

    private Fixture fixture(ExamStatus examStatus, VerifyStatus voterStatus, String suffix) {
        Subject subject = entityManager.persist(new Subject("Subject " + suffix));

        User creator = new User("Creator " + suffix, "creator-" + suffix + "@eqf.local", "hash");
        creator.setRole(UserRole.DEPARTMENT_HEAD);
        creator.setSubject(subject);
        creator.setVerifyStatus(VerifyStatus.VERIFIED);
        creator = entityManager.persist(creator);

        User voter = new User("Voter " + suffix, "voter-" + suffix + "@eqf.local", "hash");
        voter.setRole(UserRole.TEACHER);
        voter.setSubject(subject);
        voter.setVerifyStatus(voterStatus);
        voter = entityManager.persist(voter);

        Question question = new Question();
        question.setSubject(subject);
        question.setAuthor(creator);
        question.setContent("Question " + suffix);
        question.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        question.setDifficulty(DifficultyLevel.RECOGNITION);
        question.setDifficultySource(DifficultySource.AUTHOR);
        question.setStatus(QuestionStatus.IN_POOL);
        question = entityManager.persist(question);

        Exam exam = new Exam();
        exam.setTitle("Exam " + suffix);
        exam.setSubject(subject);
        exam.setTotalQuestions(1);
        exam.setCreatedBy(creator);
        exam.setStatus(examStatus);
        exam = entityManager.persist(exam);

        ExamCandidate candidate = entityManager.persist(new ExamCandidate(exam, question));
        entityManager.flush();
        return new Fixture(voter, candidate);
    }

    private CastVoteRequest voteRequest(int value, String comment) {
        CastVoteRequest request = new CastVoteRequest();
        request.setValue((short) value);
        request.setComment(comment);
        return request;
    }

    private record Fixture(User voter, ExamCandidate candidate) {}
}
