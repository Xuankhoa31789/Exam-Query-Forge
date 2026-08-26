package com.eqf.dto;

import com.eqf.model.AnswerOption;
import com.eqf.model.ExamCandidate;
import com.eqf.model.Question;
import com.eqf.model.Vote;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class VotingCandidateResponse {
    private Long id;
    private Long examId;
    private Long questionId;
    private String content;
    private String difficulty;
    private String questionType;
    private String status;
    private LocalDateTime addedAt;
    private int totalScore;
    private Short myVoteValue;
    private String myVoteComment;
    private List<OptionView> options;

    public static VotingCandidateResponse from(ExamCandidate candidate, int totalScore, Vote myVote) {
        Question question = candidate.getQuestion();
        VotingCandidateResponse response = new VotingCandidateResponse();
        response.id = candidate.getId();
        response.examId = candidate.getExam().getId();
        response.questionId = question.getId();
        response.content = question.getContent();
        response.difficulty = question.getDifficulty().name();
        response.questionType = question.getQuestionType().name();
        response.status = candidate.getStatus().name();
        response.addedAt = candidate.getAddedAt();
        // Người bình chọn cần thấy phương án trả lời mới đánh giá được chất lượng câu hỏi:
        // lỗi hay gặp nhất của câu trắc nghiệm nằm ở phương án nhiễu, không ở câu dẫn.
        response.options = question.getOptions().stream()
                .sorted(Comparator.comparing(
                        AnswerOption::getSortOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(OptionView::from)
                .toList();
        response.totalScore = totalScore;
        if (myVote != null) {
            response.myVoteValue = myVote.getValue();
            response.myVoteComment = myVote.getComment();
        }
        return response;
    }

    public Long getId() { return id; }
    public Long getExamId() { return examId; }
    public Long getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public String getDifficulty() { return difficulty; }
    public String getQuestionType() { return questionType; }
    public String getStatus() { return status; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public int getTotalScore() { return totalScore; }
    public Short getMyVoteValue() { return myVoteValue; }
    public String getMyVoteComment() { return myVoteComment; }
    public List<OptionView> getOptions() { return options; }

    /** Phương án trả lời hiển thị khi bình chọn. */
    public static class OptionView {
        private String content;
        private boolean correct;

        static OptionView from(AnswerOption option) {
            OptionView view = new OptionView();
            view.content = option.getContent();
            view.correct = option.isCorrect();
            return view;
        }

        public String getContent() { return content; }
        public boolean isCorrect() { return correct; }
    }
}
