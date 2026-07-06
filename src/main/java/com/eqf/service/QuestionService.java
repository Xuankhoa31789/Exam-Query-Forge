package com.eqf.service;

import com.eqf.dto.CreateQuestionRequest;
import com.eqf.dto.OptionDto;
import com.eqf.model.*;
import com.eqf.repository.ChapterRepository;
import com.eqf.repository.QuestionRepository;
import com.eqf.repository.SubjectRepository;
import com.eqf.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;

    public QuestionService(QuestionRepository questionRepository,
                           SubjectRepository subjectRepository,
                           ChapterRepository chapterRepository,
                           UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
    }

    /** Tạo câu hỏi mới (kèm các phương án). Trạng thái mặc định DRAFT. */
    @Transactional
    public Question create(CreateQuestionRequest req) {
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new IllegalArgumentException("Nội dung câu hỏi là bắt buộc");
        }
        if (req.getQuestionType() == null) {
            throw new IllegalArgumentException("Loại câu hỏi là bắt buộc");
        }
        if (req.getDifficulty() == null) {
            throw new IllegalArgumentException("Độ khó là bắt buộc");
        }

        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ môn id=" + req.getSubjectId()));

        User author = userRepository.findById(req.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tác giả id=" + req.getAuthorId()));

        // Chỉ giáo viên đã xác minh mới được đóng góp câu hỏi.
        if (author.getVerifyStatus() != VerifyStatus.VERIFIED) {
            throw new IllegalArgumentException("Tài khoản chưa được xác minh, không thể đóng góp câu hỏi");
        }

        Question q = new Question();
        q.setSubject(subject);
        q.setAuthor(author);
        q.setContent(req.getContent().trim());
        q.setQuestionType(req.getQuestionType());
        q.setDifficulty(req.getDifficulty());
        q.setDifficultySource(DifficultySource.AUTHOR);
        q.setStatus(QuestionStatus.DRAFT);

        if (req.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(req.getChapterId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương id=" + req.getChapterId()));
            if (!chapter.getSubject().getId().equals(subject.getId())) {
                throw new IllegalArgumentException("Chương không thuộc bộ môn đã chọn");
            }
            q.setChapter(chapter);
        }

        if (req.getOptions() != null) {
            for (OptionDto o : req.getOptions()) {
                q.addOption(new AnswerOption(o.getContent(), o.isCorrect(), o.getSortOrder()));
            }
        }

        return questionRepository.save(q);
    }

    /** Đưa câu hỏi từ DRAFT vào pool chung (IN_POOL). */
    @Transactional
    public Question publishToPool(Long questionId) {
        Question q = getById(questionId);
        if (q.getStatus() == QuestionStatus.ARCHIVED) {
            throw new IllegalArgumentException("Câu hỏi đã lưu trữ, không thể đưa vào pool");
        }
        q.setStatus(QuestionStatus.IN_POOL);
        return questionRepository.save(q);
    }

    @Transactional(readOnly = true)
    public Question getById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi id=" + id));
    }

    /** Xem pool: chỉ câu IN_POOL, lọc theo môn/chương/độ khó (tham số null = bỏ qua). */
    @Transactional(readOnly = true)
    public List<Question> listPool(Long subjectId, Long chapterId, DifficultyLevel difficulty) {
        return questionRepository.search(subjectId, chapterId, difficulty, QuestionStatus.IN_POOL);
    }
}
