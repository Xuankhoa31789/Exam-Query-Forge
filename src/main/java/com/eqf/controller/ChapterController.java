package com.eqf.controller;

import com.eqf.dto.CreateChapterRequest;
import com.eqf.model.Chapter;
import com.eqf.service.ChapterService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody CreateChapterRequest request) {
        Chapter c = chapterService.create(request);
        return toMap(c);
    }

    @GetMapping
    public List<Map<String, Object>> listBySubject(@RequestParam Long subjectId) {
        return chapterService.listBySubject(subjectId).stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toMap(Chapter c) {
        return Map.of(
                "id", c.getId(),
                "subjectId", c.getSubject().getId(),
                "name", c.getName(),
                "grade", c.getGrade() != null ? c.getGrade() : ""
        );
    }
}
