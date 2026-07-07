package com.eqf.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eqf.model.AiAnalysis;
import com.eqf.model.Question;
import com.eqf.model.Subject;
import com.eqf.repository.AiAnalysisRepository;
import com.eqf.repository.QuestionRepository;
import com.eqf.repository.SubjectRepository;

@Service
public class AiAnalysisService {

    private final DifficultyAnalyzer analyzer;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;

    public AiAnalysisService(DifficultyAnalyzer analyzer,
                             AiAnalysisRepository aiAnalysisRepository,
                             QuestionRepository questionRepository,
                             SubjectRepository subjectRepository) {
        this.analyzer = analyzer;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
    }

    /** Phân tích nội dung thô (chưa lưu) — phục vụ gợi ý tức thì trên form. */
    @Transactional(readOnly = true)
    public DifficultyAnalyzer.Result analyzeContent(String content, Long subjectId) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Nội dung câu hỏi trống, không thể phân tích");
        }
        String subjectName = null;
        if (subjectId != null) {
            subjectName = subjectRepository.findById(subjectId).map(Subject::getName).orElse(null);
        }
        return analyzer.analyze(content, subjectName);
    }

    /** Phân tích một câu hỏi đã lưu VÀ ghi lại kết quả vào ai_analyses. */
    @Transactional
    public DifficultyAnalyzer.Result analyzeAndStore(Long questionId) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Not found id=" + questionId));
        String subjectName = q.getSubject() != null ? q.getSubject().getName() : null;

        DifficultyAnalyzer.Result r = analyzer.analyze(q.getContent(), subjectName);
        aiAnalysisRepository.save(new AiAnalysis(q, r.difficulty(), r.reasoning(), r.modelName()));
        return r;
    }
}
