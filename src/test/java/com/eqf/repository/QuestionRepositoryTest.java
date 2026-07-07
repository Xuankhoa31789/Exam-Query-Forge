package com.eqf.repository;

import com.eqf.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class QuestionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void pullCandidatesFiltersCooldownOrdersByOldestUsageAndAppliesLimit() {
        Subject subject = entityManager.persist(new Subject("Toán kiểm thử"));
        User author = new User("Giáo viên kiểm thử", "repo-test@eqf.local", "hash");
        author.setSubject(subject);
        author.setVerifyStatus(VerifyStatus.VERIFIED);
        author = entityManager.persist(author);

        Question neverUsed = entityManager.persist(question(subject, author, "Chưa từng dùng"));
        Question usedLongAgo = entityManager.persist(question(subject, author, "Đã dùng từ lâu"));
        Question inCooldown = entityManager.persist(question(subject, author, "Đang cooldown"));

        Question draft = question(subject, author, "Còn là bản nháp");
        draft.setStatus(QuestionStatus.DRAFT);
        entityManager.persist(draft);

        Question otherDifficulty = question(subject, author, "Khác độ khó");
        otherDifficulty.setDifficulty(DifficultyLevel.APPLICATION);
        entityManager.persist(otherDifficulty);

        Subject otherSubject = entityManager.persist(new Subject("Vật lý kiểm thử"));
        entityManager.persist(question(otherSubject, author, "Khác bộ môn"));

        Exam exam = new Exam();
        exam.setTitle("Đề kiểm thử");
        exam.setSubject(subject);
        exam.setTotalQuestions(10);
        exam.setCreatedBy(author);
        exam = entityManager.persist(exam);

        LocalDateTime now = LocalDateTime.now();
        entityManager.persist(new QuestionUsageHistory(usedLongAgo, exam, now.minusDays(90)));
        entityManager.persist(new QuestionUsageHistory(inCooldown, exam, now.minusDays(5)));
        entityManager.flush();

        List<Question> result = questionRepository.pullCandidates(
                subject.getId(),
                DifficultyLevel.RECOGNITION,
                now.minusDays(30),
                10
        );

        assertThat(result)
                .extracting(Question::getContent)
                .containsExactly("Chưa từng dùng", "Đã dùng từ lâu");

        List<Question> limited = questionRepository.pullCandidates(
                subject.getId(),
                DifficultyLevel.RECOGNITION,
                now.minusDays(30),
                1
        );

        assertThat(limited)
                .extracting(Question::getContent)
                .containsExactly("Chưa từng dùng");
        assertThat(questionRepository.pullCandidates(
                subject.getId(), DifficultyLevel.RECOGNITION, now.minusDays(30), 0
        )).isEmpty();
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
}
