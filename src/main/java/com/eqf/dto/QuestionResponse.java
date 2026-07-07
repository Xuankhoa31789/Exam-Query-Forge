package com.eqf.dto;

import com.eqf.model.AnswerOption;
import com.eqf.model.Question;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response phẳng cho câu hỏi — tránh trả entity trực tiếp
 * (sẽ gặp lỗi lazy-loading / vòng lặp khi serialize JSON).
 */
public class QuestionResponse {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private Long chapterId;
    private String chapterName;
    private Long authorId;
    private String authorName;
    private String content;
    private String questionType;
    private String difficulty;
    private String difficultySource;
    private String status;
    private List<OptionResponse> options;

    public static QuestionResponse from(Question q) {
        QuestionResponse r = new QuestionResponse();
        r.id = q.getId();
        r.subjectId = q.getSubject() != null ? q.getSubject().getId() : null;
        r.subjectName = q.getSubject() != null ? q.getSubject().getName() : null;
        r.chapterId = q.getChapter() != null ? q.getChapter().getId() : null;
        r.chapterName = q.getChapter() != null ? q.getChapter().getName() : null;
        r.authorId = q.getAuthor() != null ? q.getAuthor().getId() : null;
        r.authorName = q.getAuthor() != null ? q.getAuthor().getFullName() : null;
        r.content = q.getContent();
        r.questionType = q.getQuestionType() != null ? q.getQuestionType().name() : null;
        r.difficulty = q.getDifficulty() != null ? q.getDifficulty().name() : null;
        r.difficultySource = q.getDifficultySource() != null ? q.getDifficultySource().name() : null;
        r.status = q.getStatus() != null ? q.getStatus().name() : null;
        r.options = q.getOptions().stream().map(OptionResponse::from).collect(Collectors.toList());
        return r;
    }

    public Long getId() { return id; }
    public Long getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public Long getChapterId() { return chapterId; }
    public String getChapterName() { return chapterName; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getContent() { return content; }
    public String getQuestionType() { return questionType; }
    public String getDifficulty() { return difficulty; }
    public String getDifficultySource() { return difficultySource; }
    public String getStatus() { return status; }
    public List<OptionResponse> getOptions() { return options; }

    /** Phương án trả lời ở dạng response. */
    public static class OptionResponse {
        private Long id;
        private String content;
        private boolean isCorrect;
        private Integer sortOrder;

        public static OptionResponse from(AnswerOption o) {
            OptionResponse r = new OptionResponse();
            r.id = o.getId();
            r.content = o.getContent();
            r.isCorrect = o.isCorrect();
            r.sortOrder = o.getSortOrder();
            return r;
        }

        public Long getId() { return id; }
        public String getContent() { return content; }
        public boolean isCorrect() { return isCorrect; }
        public Integer getSortOrder() { return sortOrder; }
    }
}
