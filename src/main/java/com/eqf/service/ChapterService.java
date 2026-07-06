package com.eqf.service;

import com.eqf.dto.CreateChapterRequest;
import com.eqf.model.Chapter;
import com.eqf.model.Subject;
import com.eqf.repository.ChapterRepository;
import com.eqf.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;

    public ChapterService(ChapterRepository chapterRepository, SubjectRepository subjectRepository) {
        this.chapterRepository = chapterRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public Chapter create(CreateChapterRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Tên chương là bắt buộc");
        }
        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ môn id=" + req.getSubjectId()));
        return chapterRepository.save(new Chapter(subject, req.getName().trim(), req.getGrade()));
    }

    @Transactional(readOnly = true)
    public List<Chapter> listBySubject(Long subjectId) {
        return chapterRepository.findBySubjectId(subjectId);
    }
}
