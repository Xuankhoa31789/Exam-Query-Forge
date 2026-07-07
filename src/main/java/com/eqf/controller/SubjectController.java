package com.eqf.controller;

import com.eqf.repository.SubjectRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Endpoint đọc danh sách bộ môn — cho dropdown ở giao diện. */
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectRepository subjectRepository;

    public SubjectController(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return subjectRepository.findAll().stream()
                .map(s -> Map.<String, Object>of("id", s.getId(), "name", s.getName()))
                .collect(Collectors.toList());
    }
}
